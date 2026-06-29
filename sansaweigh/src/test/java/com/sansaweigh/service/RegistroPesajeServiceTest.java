package com.sansaweigh.service;

import com.sansaweigh.exception.BalanzaPrimaException;
import com.sansaweigh.exception.IllegalWeighingStateException;
import com.sansaweigh.exception.PesajeNocturnoException;
import com.sansaweigh.exception.RegistroNoEncontradoException;
import com.sansaweigh.integration.ExternalScaleClient;
import com.sansaweigh.model.CategoriaPeso;
import com.sansaweigh.model.EstadoPesaje;
import com.sansaweigh.model.RegistroPesaje;
import com.sansaweigh.model.TransicionEstado;
import com.sansaweigh.repository.RegistroPesajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPesajeServiceTest {

    @Mock
    private RegistroPesajeRepository repository;

    @Mock
    private ExternalScaleClient externalScaleClient;

    private RegistroPesajeService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-06-15T10:00:00Z"), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        service = new RegistroPesajeService(repository, externalScaleClient);
        ReflectionTestUtils.setField(service, "clock", clock);
    }

    @Test
    void crearRegistroLiviano() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.crearRegistro("101", "PKG-001", 8.0);

        assertThat(resultado.getCategoria()).isEqualTo(CategoriaPeso.LIVIANO);
        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.INGRESADO);
        assertThat(resultado.getHistorialEstados()).hasSize(1);
        assertThat(resultado.getHistorialEstados().get(0).getEstado()).isEqualTo(EstadoPesaje.INGRESADO);
        assertThat(resultado.getCreatedAt()).isNotNull();
        assertThat(resultado.getUpdatedAt()).isNotNull();

        verify(repository).save(any());
        verifyNoInteractions(externalScaleClient);
    }

    @Test
    void crearRegistroMediano() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.crearRegistro("99", "PKG-002", 30.0);

        assertThat(resultado.getCategoria()).isEqualTo(CategoriaPeso.MEDIANO);
        verifyNoInteractions(externalScaleClient);
    }

    @Test
    void crearRegistroPesadoLlamaAlClienteExterno() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(externalScaleClient.getScaleSpecifications("10")).thenReturn(null);

        service.crearRegistro("10", "PKG-003", 75.0);

        verify(externalScaleClient).getScaleSpecifications("10");
    }

    @Test
    void crearRegistroPesadoConBalanzaPrimaEnDiaImpar() {
        assertThatThrownBy(() -> service.crearRegistro("7", "PKG-004", 75.0))
                .isInstanceOf(BalanzaPrimaException.class)
                .hasMessageContaining("ID primo");
    }

    @Test
    void pesarRegistro() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.pesarRegistro("123");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.PESADO);
        assertThat(resultado.getHistorialEstados()).hasSize(2);
        assertThat(resultado.getHistorialEstados().get(1).getEstado()).isEqualTo(EstadoPesaje.PESADO);
    }

    @Test
    void pesarRegistroConEstadoInvalidoLanzaExcepcion() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.APROBADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.pesarRegistro("123"))
                .isInstanceOf(IllegalWeighingStateException.class)
                .hasMessageContaining("PESADO");
    }

    @Test
    void pesarRegistroConCategoriaPesado() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.PESADO);
        registro.setIdBalanza("4");
        when(repository.findById("123")).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.pesarRegistro("123");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.PESADO);
    }

    @Test
    void aprobarRegistro() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.PESADO, CategoriaPeso.MEDIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.aprobarRegistro("123");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.APROBADO);
    }

    @Test
    void aprobarRegistroDesdeIngresadoLanzaExcepcion() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.aprobarRegistro("123"))
                .isInstanceOf(IllegalWeighingStateException.class);
    }

    @Test
    void rechazarRegistro() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.PESADO, CategoriaPeso.PESADO);
        registro.setIdBalanza("4");
        when(repository.findById("123")).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.rechazarRegistro("123");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.RECHAZADO);
    }

    @Test
    void rechazarRegistroDesdeEstadoInvalidoLanzaExcepcion() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.rechazarRegistro("123"))
                .isInstanceOf(IllegalWeighingStateException.class)
                .hasMessageContaining("RECHAZADO");
    }

    @Test
    void despacharRegistro() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.APROBADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.despacharRegistro("123");

        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.DESPACHADO);
    }

    @Test
    void despacharRegistroNoAprobadoLanzaExcepcion() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.RECHAZADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.despacharRegistro("123"))
                .isInstanceOf(IllegalWeighingStateException.class);
    }

    @Test
    void obtenerPorIdExistente() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));

        RegistroPesaje resultado = service.obtenerPorId("123");

        assertThat(resultado).isEqualTo(registro);
    }

    @Test
    void obtenerPorIdInexistenteLanzaExcepcion() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId("999"))
                .isInstanceOf(RegistroNoEncontradoException.class)
                .hasMessageContaining("999");
    }

    @Test
    void obtenerTodos() {
        List<RegistroPesaje> registros = List.of(
                registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.LIVIANO),
                registroBuilder(EstadoPesaje.PESADO, CategoriaPeso.MEDIANO)
        );
        when(repository.findAll()).thenReturn(registros);

        List<RegistroPesaje> resultado = service.obtenerTodos();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void obtenerRegistrosPorFechas() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);
        when(repository.findByCreatedAtBetween(any(), any())).thenReturn(List.of());

        List<RegistroPesaje> resultado = service.obtenerRegistros(desde, hasta);

        verify(repository).findByCreatedAtBetween(
                LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTime.of(2026, 1, 31, 23, 59, 59));
        assertThat(resultado).isEmpty();
    }

    @Test
    void actualizarRegistro() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.INGRESADO, CategoriaPeso.LIVIANO);
        registro.setPesoEnSansas(5.0);
        when(repository.findById("123")).thenReturn(Optional.of(registro));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.actualizarRegistro("123", 45.0);

        assertThat(resultado.getPesoEnSansas()).isEqualTo(45.0);
        assertThat(resultado.getCategoria()).isEqualTo(CategoriaPeso.MEDIANO);
    }

    @Test
    void actualizarRegistroNoIngresadoLanzaExcepcion() {
        RegistroPesaje registro = registroBuilder(EstadoPesaje.PESADO, CategoriaPeso.LIVIANO);
        when(repository.findById("123")).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> service.actualizarRegistro("123", 45.0))
                .isInstanceOf(IllegalWeighingStateException.class)
                .hasMessageContaining("INGRESADO");
    }

    @Test
    void crearRegistroConBalanzaNoNumericaIgnoraPrima() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroPesaje resultado = service.crearRegistro("ABC", "PKG-001", 75.0);

        assertThat(resultado.getEstado()).isEqualTo(EstadoPesaje.INGRESADO);
        verifyNoInteractions(externalScaleClient);
    }

    @Test
    void crearRegistroPesadoEnHorarioNocturno() {
        RegistroPesajeService nocturnalService = new RegistroPesajeService(repository, externalScaleClient);
        Clock nocturnalClock = Clock.fixed(
                Instant.parse("2026-06-15T23:00:00Z"), ZoneId.of("UTC"));
        ReflectionTestUtils.setField(nocturnalService, "clock", nocturnalClock);

        assertThatThrownBy(() -> nocturnalService.crearRegistro("4", "PKG-001", 75.0))
                .isInstanceOf(PesajeNocturnoException.class)
                .hasMessageContaining("20:00 y 06:00");
    }

    @Test
    void crearRegistroPesadoDeMadrugada() {
        RegistroPesajeService madrugadaService = new RegistroPesajeService(repository, externalScaleClient);
        Clock madrugadaClock = Clock.fixed(
                Instant.parse("2026-06-15T03:00:00Z"), ZoneId.of("UTC"));
        ReflectionTestUtils.setField(madrugadaService, "clock", madrugadaClock);

        assertThatThrownBy(() -> madrugadaService.crearRegistro("4", "PKG-001", 75.0))
                .isInstanceOf(PesajeNocturnoException.class)
                .hasMessageContaining("20:00 y 06:00");
    }

    private RegistroPesaje registroBuilder(EstadoPesaje estado, CategoriaPeso categoria) {
        RegistroPesaje registro = RegistroPesaje.builder()
                .id("123")
                .idBalanza("101")
                .idPaquete("PKG-001")
                .pesoEnSansas(categoria == CategoriaPeso.PESADO ? 75.0 : 8.0)
                .categoria(categoria)
                .estado(estado)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        registro.getHistorialEstados().add(new TransicionEstado(estado, LocalDateTime.now()));
        return registro;
    }
}

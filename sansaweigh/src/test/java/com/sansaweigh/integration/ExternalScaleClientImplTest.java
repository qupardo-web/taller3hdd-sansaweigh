package com.sansaweigh.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalScaleClientImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RedisTemplate<String, ScaleSpecification> redisTemplate;

    @Mock
    private ValueOperations<String, ScaleSpecification> valueOperations;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ExternalScaleClientImpl client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        client = new ExternalScaleClientImpl(restClient, redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getScaleSpecificationsApiExitosa() {
        ScaleSpecification expected = new ScaleSpecification("101", "Balanza X", "Marca", 150.0, 0.01, -0.05);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class)).thenReturn(expected);

        ScaleSpecification result = client.getScaleSpecifications("101");

        assertThat(result).isEqualTo(expected);
        verify(valueOperations).set(eq("scale:101"), eq(expected), eq(Duration.ofSeconds(120)));
    }

    @Test
    void getScaleSpecificationsApiRespondeEnSegundoIntento() {
        ScaleSpecification expected = new ScaleSpecification("101", "Balanza X", "Marca", 150.0, 0.01, -0.05);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class))
                .thenThrow(new RuntimeException("fallo red"))
                .thenReturn(expected);

        ScaleSpecification result = client.getScaleSpecifications("101");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getScaleSpecificationsApiExitosaPeroRedisCacheFalla() {
        ScaleSpecification expected = new ScaleSpecification("101", "Balanza X", "Marca", 150.0, 0.01, -0.05);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class)).thenReturn(expected);
        doThrow(new RuntimeException("Redis caído")).when(valueOperations)
                .set(eq("scale:101"), eq(expected), eq(Duration.ofSeconds(120)));

        ScaleSpecification result = client.getScaleSpecifications("101");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getScaleSpecificationsUsaCacheCuandoApiFalla() {
        ScaleSpecification cached = new ScaleSpecification("101", "Cache", "Marca", 100.0, 0.01, 0.0);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class)).thenThrow(new RuntimeException("API no disponible"));

        when(valueOperations.get("scale:101")).thenReturn(cached);

        ScaleSpecification result = client.getScaleSpecifications("101");

        assertThat(result).isEqualTo(cached);
    }

    @Test
    void getScaleSpecificationsUsaDefaultCuandoCacheVacia() {
        ScaleSpecification defaultSpec = new ScaleSpecification("-1", "Balanza Default", "Genérica", 100.0, 0.1, 0.0);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class)).thenThrow(new RuntimeException("API no disponible"));

        when(valueOperations.get("scale:999")).thenReturn(null);
        when(valueOperations.get("scale:-1")).thenReturn(defaultSpec);

        ScaleSpecification result = client.getScaleSpecifications("999");

        assertThat(result).isEqualTo(defaultSpec);
    }

    @Test
    void getScaleSpecificationsFallbackHardcodeado() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class)).thenThrow(new RuntimeException("API no disponible"));

        when(valueOperations.get("scale:999")).thenThrow(new RuntimeException("Redis no disponible"));
        when(valueOperations.get("scale:-1")).thenThrow(new RuntimeException("Redis no disponible"));

        ScaleSpecification result = client.getScaleSpecifications("999");

        assertThat(result.getId()).isEqualTo("-1");
        assertThat(result.getName()).isEqualTo("Balanza Default");
        assertThat(result.getBrand()).isEqualTo("Genérica");
    }

    @Test
    void getScaleSpecificationsInterrumpidoDuranteSleep() {
        ScaleSpecification cached = new ScaleSpecification("101", "Cache", "Marca", 100.0, 0.01, 0.0);

        ExternalScaleClientImpl interruptedClient = new ExternalScaleClientImpl(restClient, redisTemplate) {
            @Override
            void doSleep(long millis) throws InterruptedException {
                throw new InterruptedException("sleep interrumpido");
            }
        };

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ScaleSpecification.class)).thenThrow(new RuntimeException("API no disponible"));
        when(valueOperations.get("scale:101")).thenReturn(cached);

        ScaleSpecification result = interruptedClient.getScaleSpecifications("101");

        assertThat(result).isEqualTo(cached);
    }
}

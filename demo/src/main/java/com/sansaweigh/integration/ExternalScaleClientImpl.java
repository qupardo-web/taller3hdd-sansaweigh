package com.sansaweigh.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class ExternalScaleClientImpl implements ExternalScaleClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalScaleClientImpl.class);

    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 500;
    private static final String REDIS_KEY_PREFIX = "scale:";
    private static final String DEFAULT_SCALE_ID = "-1";
    private static final Duration CACHE_TTL = Duration.ofSeconds(120);

    private final RestClient restClient;
    private final RedisTemplate<String, ScaleSpecification> redisTemplate;

    public ExternalScaleClientImpl(RestClient restClient,
                                   RedisTemplate<String, ScaleSpecification> redisTemplate) {
        this.restClient = restClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ScaleSpecification getScaleSpecifications(String scaleId) {
        ScaleSpecification spec = fetchWithRetries(scaleId);

        if (spec != null) {
            cacheSpecification(scaleId, spec);
            return spec;
        }

        spec = getCachedSpecification(scaleId);
        if (spec != null) {
            log.info("Usando especificación en caché para scaleId={}", scaleId);
            return spec;
        }

        log.warn("Usando especificación por defecto para scaleId={}", scaleId);
        return getDefaultSpecification();
    }

    private ScaleSpecification fetchWithRetries(String scaleId) {
        long delay = BASE_DELAY_MS;

        for (int intento = 1; intento <= MAX_RETRIES; intento++) {
            try {
                log.info("Intento {}/{} consultando API externa para scaleId={}", intento, MAX_RETRIES, scaleId);
                return restClient.get()
                        .uri("/specifications/{id}", scaleId)
                        .retrieve()
                        .body(ScaleSpecification.class);
            } catch (Exception e) {
                log.error("Intento {} falló para scaleId={}: {}", intento, scaleId, e.getMessage());
                if (intento == MAX_RETRIES) {
                    break;
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                delay *= 2;
            }
        }
        return null;
    }

    private void cacheSpecification(String scaleId, ScaleSpecification spec) {
        try {
            String key = REDIS_KEY_PREFIX + scaleId;
            redisTemplate.opsForValue().set(key, spec, CACHE_TTL);
        } catch (Exception e) {
            log.warn("No se pudo guardar en caché Redis para scaleId={}", scaleId);
        }
    }

    private ScaleSpecification getCachedSpecification(String scaleId) {
        try {
            String key = REDIS_KEY_PREFIX + scaleId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("No se pudo leer de caché Redis para scaleId={}", scaleId);
            return null;
        }
    }

    private ScaleSpecification getDefaultSpecification() {
        try {
            String key = REDIS_KEY_PREFIX + DEFAULT_SCALE_ID;
            ScaleSpecification defaultSpec = redisTemplate.opsForValue().get(key);
            if (defaultSpec != null) {
                return defaultSpec;
            }
        } catch (Exception e) {
            log.warn("No se pudo leer especificación por defecto de Redis");
        }
        return new ScaleSpecification(DEFAULT_SCALE_ID, "Balanza Default", "Genérica", 100.0, 0.1, 0.0);
    }
}

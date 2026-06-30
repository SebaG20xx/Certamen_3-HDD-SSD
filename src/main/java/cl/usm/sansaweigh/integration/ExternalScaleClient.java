package cl.usm.sansaweigh.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Component
public class ExternalScaleClient {

    private static final int MAX_REINTENTOS = 3;
    private static final String CACHE_PREFIX = "scale:";
    private static final String CACHE_KEY_DEFAULT = CACHE_PREFIX + "-1";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${scale.api.url}")
    private String scaleApiUrl;

    public EspecificacionBalanza getScaleSpecifications(String scaleId) {
        try {
            EspecificacionBalanza spec = llamarApiConReintentos(scaleId);
            guardarEnCache(scaleId, spec);
            return spec;
        } catch (Exception ex) {
            return obtenerDesdeCache(scaleId);
        }
    }

    private EspecificacionBalanza llamarApiConReintentos(String scaleId) throws Exception {
        Exception ultimaExcepcion = null;
        for (int intento = 0; intento <= MAX_REINTENTOS; intento++) {
            try {
                if (intento > 0) {
                    Thread.sleep(1000L * (long) Math.pow(2, intento - 1));
                }
                return restTemplate.getForObject(scaleApiUrl + scaleId, EspecificacionBalanza.class);
            } catch (Exception ex) {
                ultimaExcepcion = ex;
            }
        }
        throw ultimaExcepcion;
    }

    private void guardarEnCache(String scaleId, EspecificacionBalanza spec) {
        try {
            redisTemplate.opsForValue().set(CACHE_PREFIX + scaleId, spec, 120, TimeUnit.SECONDS);
        } catch (Exception ex) {
            // Si falla el guardado en caché, no interrumpimos el flujo
        }
    }

    private EspecificacionBalanza obtenerDesdeCache(String scaleId) {
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_PREFIX + scaleId);
            if (cached instanceof EspecificacionBalanza spec) {
                return spec;
            }
            Object defaultSpec = redisTemplate.opsForValue().get(CACHE_KEY_DEFAULT);
            if (defaultSpec instanceof EspecificacionBalanza spec) {
                return spec;
            }
        } catch (Exception ex) {
            // Si falla la lectura del caché, retornamos null
        }
        return null;
    }
}

package cl.usm.sansaweigh.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
            String json = objectMapper.writeValueAsString(spec);
            redisTemplate.opsForValue().set(CACHE_PREFIX + scaleId, json, 120, TimeUnit.SECONDS);
        } catch (Exception ex) {
            // Si falla el guardado en caché, no interrumpimos el flujo
        }
    }

    private EspecificacionBalanza obtenerDesdeCache(String scaleId) {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_PREFIX + scaleId);
            if (json != null) {
                return objectMapper.readValue(json, EspecificacionBalanza.class);
            }
            String jsonDefault = redisTemplate.opsForValue().get(CACHE_KEY_DEFAULT);
            if (jsonDefault != null) {
                return objectMapper.readValue(jsonDefault, EspecificacionBalanza.class);
            }
        } catch (Exception ex) {
            // Si falla la lectura del caché, retornamos null
        }
        return null;
    }
}

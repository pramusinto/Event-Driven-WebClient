package com.scoring.service.poc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scoring.config.RedisProperties;
import com.scoring.model.TempReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class RedisService {

    private final ObjectMapper mapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void save(Long reportId, Map<String, Object> value) {
        try {
            String key = "report:"+reportId;
            String valueRedis = mapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, valueRedis, 1, TimeUnit.DAYS);
        } catch (Exception e){
            log.error("[] Error when parsing json to string");
            log.info("Msg : {}", e.getMessage());
        }
    }

    public void saveTemplate(Long reportId, Map<String, Object> value) {
        try {
            String key = "template:"+reportId;
            String valueRedis = mapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, valueRedis, 1, TimeUnit.DAYS);
        } catch (Exception e){
            log.error("[] Error when parsing json template to string");
            log.info("Msg : {}", e.getMessage());
        }
    }

    public Map<String, Object> read(Long  reportId) {
        try {
            String key = "report:"+reportId;
            String resut = redisTemplate.opsForValue().get(key);
            return mapper.readValue(resut, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validationOnRedis(Long reportId){
        String key = "report:"+reportId;
        String result = redisTemplate.opsForValue().get(key);
        boolean resultRedis = result != null;
        log.debug("is Redis Found : {}", resultRedis);
        return resultRedis;
    }
}

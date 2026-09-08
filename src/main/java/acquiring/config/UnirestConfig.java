package acquiring.config;

import com.google.gson.*;
import jakarta.annotation.PostConstruct;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Configuration
@Slf4j
public class UnirestConfig {
    @PostConstruct
    void init() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
                    @Override
                    public JsonElement serialize(OffsetDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .create();

        log.info("Start configuring Unirest...");
        Unirest.config().setObjectMapper(new ObjectMapper() {
            private final Gson gson = new GsonBuilder()
                    .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>)
                            (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                    .registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {
                        @Override
                        public JsonElement serialize(LocalDate src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
                            return new JsonPrimitive(src.toString()); // yyyy-MM-dd
                        }
                    })
                    .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
                        @Override
                        public LocalDate deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context)
                                throws JsonParseException {
                            return LocalDate.parse(json.getAsString());
                        }
                    })
                    .create();

            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                return gson.fromJson(value, valueType);
            }

            @Override
            public String writeValue(Object value) {
                return gson.toJson(value);
            }
        });
        Unirest.config()
                .connectTimeout(7000)
                .connectTimeout(7000)
                .concurrency(10, 5)
                .followRedirects(false)
                .enableCookieManagement(false)
                .verifySsl(false);
        log.info("End configuring Unirest.");
    }
}


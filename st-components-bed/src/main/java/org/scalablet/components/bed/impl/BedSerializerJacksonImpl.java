package org.scalablet.components.bed.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.scalablet.components.bed.BedExecutorCmd;
import org.scalablet.components.bed.core.BedSerializer;

import java.util.Objects;

/**
 * Serializer implementation using jackson
 *
 * @author abomb4 2021-11-10 15:24:33
 */
public class BedSerializerJacksonImpl implements BedSerializer {

    /** Jackson ObjectMapper */
    private final ObjectMapper objectMapper;

    /**
     * Construct with default jackson configuration
     */
    public BedSerializerJacksonImpl() {
        objectMapper = getObjectMapper();
    }

    /**
     * Construct with customized jackson ObjectMapper
     *
     * @param objectMapper jackson
     */
    public BedSerializerJacksonImpl(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public String serialize(BedExecutorCmd cmd) {
        try {
            return objectMapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            // no need to show full stacktrace
            throw new BedJsonException(e.getMessage(), "serialize", 76);
        }
    }

    @Override
    public <T extends BedExecutorCmd> T deserialize(String s, Class<T> type) {
        try {
            return objectMapper.readValue(s, type);
        } catch (JsonProcessingException e) {
            // no need to show full stacktrace
            throw new BedJsonException(e.getMessage(), "deserialize", 86);
        }
    }

    /** Json exception */
    static class BedJsonException extends BedSerializeException {

        /**
         * Construct with a one line stacktrace
         *
         * @param message    exception message
         * @param methodName method name
         * @param lineNumber line number
         */
        public BedJsonException(String message, String methodName, int lineNumber) {
            super(message);
            this.setStackTrace(new StackTraceElement[]{
                    new StackTraceElement(BedSerializerJacksonImpl.class.getName(),
                            methodName, "BedSerializerJacksonImpl.java", lineNumber)
            });
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * Build an jackson ObjectMapper
     *
     * @return jackson
     */
    private static ObjectMapper getObjectMapper() {
        final ObjectMapper om = new ObjectMapper();
        // java.time
        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        om.registerModule(javaTimeModule);
        // exclude null fields
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // insensitive enum names
        om.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        // ignore some fail conditions
        om.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        om.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        om.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        om.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        om.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false);
        om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, false);
        om.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        om.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false);

        return om;
    }
}

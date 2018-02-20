/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wegas.core.rest.util;

import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.persistence.JsonSerializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wegas.core.persistence.WegasJsonTypeName;

/**
 *
 * @author maxence
 */
public class WegasJsonSerializer implements JsonbDeserializer<JsonSerializable> {

    private static final Logger logger = LoggerFactory.getLogger(WegasJsonSerializer.class);

    private static final Map<String, Class<? extends JsonSerializable>> classesMap = new HashMap<>();

    public WegasJsonSerializer() {
        logger.error("CREATE PET DESERIALIZER");
    }

    private static final Reflections reflections;

    static {
        reflections = new Reflections("com.wegas");
    }

    private boolean contains(String[] values, String needle) {
        for (String v : values) {
            if (v.equals(needle)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JsonSerializable deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {

        logger.error("Deserialize {}", parser);
        Optional<Map.Entry<String, JsonValue>> klass = parser.getObjectStream().filter(entry -> entry.getKey().equals("@class")).findFirst();
        logger.error("Deserialze Klass {}", klass);
        if (klass.isPresent()) {
            JsonValue jsonClassName = klass.get().getValue();

            if (jsonClassName.getValueType() == JsonValue.ValueType.STRING) {
                String strKlass = ((JsonString) jsonClassName).getString();
                logger.error("-> {}", strKlass);

                Class<? extends JsonSerializable> theClass = classesMap.get(strKlass);

                if (theClass == null) {

                    Optional<Class<?>> findAnnotated = reflections.getTypesAnnotatedWith(WegasJsonTypeName.class).stream().filter(cl -> contains(cl.getAnnotation(WegasJsonTypeName.class).value(), strKlass)).findFirst();
                    if (findAnnotated.isPresent()) {
                        if (JsonSerializable.class.isAssignableFrom(findAnnotated.get())) {
                            logger.error("ANNOTATION REFLECTION -> {}", findAnnotated);
                            theClass = (Class<? extends JsonSerializable>) findAnnotated.get();
                        } else {
                            logger.error("ANNOTATION REFLECTION IS NOT JSON SERIALIZABLE -> {}", findAnnotated);
                        }
                    }

                    if (theClass == null) {
                        Optional<Class<? extends JsonSerializable>> eClass = reflections.getSubTypesOf(JsonSerializable.class).stream().filter(cl -> cl.getSimpleName().equals(strKlass)).findFirst();
                        if (eClass.isPresent()) {
                            logger.error("REFLECTION -> {}", eClass);
                            theClass = eClass.get();
                        }
                    }

                    if (theClass == null) {
                        classesMap.put(strKlass, theClass);
                    } else {
                        logger.error("{} fails to resolve", strKlass);
                        throw WegasErrorMessage.error("Not able to deserialise " + strKlass);
                    }
                }

                return ctx.deserialize(theClass, parser);
            }
        }

        throw new RuntimeException("oups");
    }
}

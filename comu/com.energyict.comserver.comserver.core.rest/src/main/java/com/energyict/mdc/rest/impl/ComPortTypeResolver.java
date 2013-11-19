package com.energyict.mdc.rest.impl;

import java.util.Collection;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;

public class ComPortTypeResolver implements TypeResolverBuilder<ComPortTypeResolver> {

    @Override
    public Class<?> getDefaultImpl() {
        return null;
    }

    @Override
    public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes, BeanProperty property) {
        return null;
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes, BeanProperty property) {
        return null;
    }

    @Override
    public ComPortTypeResolver init(JsonTypeInfo.Id idType, TypeIdResolver res) {
        return null;
    }

    @Override
    public ComPortTypeResolver inclusion(JsonTypeInfo.As includeAs) {
        return null;
    }

    @Override
    public ComPortTypeResolver typeProperty(String propName) {
        return null;
    }

    @Override
    public ComPortTypeResolver defaultImpl(Class<?> defaultImpl) {
        return null;
    }
}

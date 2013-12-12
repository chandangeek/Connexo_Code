package com.elster.jupiter.rest.whiteboard.impl;

import javax.ws.rs.ext.ContextResolver;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
	
	private final ObjectMapper mapper; 
	
	public ObjectMapperProvider() {
		mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
	    AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
	    AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
		mapper.setDeserializationConfig(mapper.getDeserializationConfig().with(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
		mapper.setAnnotationIntrospector(pair);
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {		
		return mapper;
	}

}

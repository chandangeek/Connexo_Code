package com.elster.jupiter.rest.whiteboard.impl;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
	
	private final ObjectMapper mapper; 
	
	public ObjectMapperProvider() {
		mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
	    AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
	    AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setAnnotationIntrospector(pair);
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {		
		return mapper;
	}

}

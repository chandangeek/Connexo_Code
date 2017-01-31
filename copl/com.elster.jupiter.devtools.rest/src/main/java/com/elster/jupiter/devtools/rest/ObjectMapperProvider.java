/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import java.io.IOException;
import java.time.Instant;
import javax.ws.rs.ext.ContextResolver;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
	
	private final ObjectMapper mapper; 
	
	public ObjectMapperProvider() {	
		mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
	    AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
	    AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setAnnotationIntrospector(pair);
		mapper.registerModule(new SimpleModule("Instant", new Version(1,0,0,null, "com.elster.jupiter", "devtools.rest"))
			.addSerializer(new InstantSerializer())
			.addDeserializer(Instant.class, new InstantDeserializer()));
	
	}	

	@Override
	public ObjectMapper getContext(Class<?> arg0) {		
		return mapper;
	}

	private static class InstantSerializer extends StdSerializer<Instant> {
		
		protected InstantSerializer() {
			super(Instant.class);
		}

		@Override
		public void serialize(Instant instant, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
			generator.writeNumber(instant.toEpochMilli());
		}

	}
	
	private static class InstantDeserializer extends StdDeserializer<Instant> {
		
		static final long serialVersionUID = 1L;
		
		protected InstantDeserializer() {
			super(Instant.class);
		}

		@Override
		public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
			return Instant.ofEpochMilli(parser.getLongValue());
		}
	}
}

package com.elster.jupiter.rest.whiteboard.impl;

import java.io.IOException;
import java.time.Instant;

import javax.ws.rs.ext.ContextResolver;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;


public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
	
	private final ObjectMapper mapper; 
	
	public ObjectMapperProvider() {
		mapper = new ObjectMapper();
		AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
	    AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
	    AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);
		mapper.setSerializationInclusion(Inclusion.NON_NULL);
		mapper.setAnnotationIntrospector(pair);
		mapper.registerModule(new SimpleModule("Instant", new Version(1,0,0,null))
			.addSerializer(new InstantSerializer())
			.addDeserializer(Instant.class, new InstantDeserializer()));
	}

	@Override
	public ObjectMapper getContext(Class<?> arg0) {		
		return mapper;
	}

	private static class InstantSerializer extends SerializerBase<Instant> {
		
		protected InstantSerializer() {
			super(Instant.class);
		}

		@Override
		public void serialize(Instant instant, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
			generator.writeNumber(instant.toEpochMilli());
		}

	}
	
	private static class InstantDeserializer extends StdDeserializer<Instant> {
		
		protected InstantDeserializer() {
			super(Instant.class);
		}

		@Override
		public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
			return Instant.ofEpochMilli(parser.getLongValue());
		}
	}
}

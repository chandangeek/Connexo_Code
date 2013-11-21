package com.energyict.mdc.rest.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComServerResourceTest extends JerseyTest {

    @Path("hello")
       public static class HelloResource {
           @GET
           public String getHello() {
               return "Hello World!";
           }
       }

       @Override
       protected Application configure() {
           return new ResourceConfig(HelloResource.class);
       }

       @Test
       public void test() {
           final String hello = target("hello").request().get(String.class);
           assertEquals("Hello World!", hello);
       }
   }
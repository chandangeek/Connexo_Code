package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.PROPFIND;
import org.junit.Test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Created by bvn on 10/9/15.
 */
public class AnnotationsTest extends MultisensePublicApiJerseyTest {
    @Test
    public void testAllRestMethodsHaveRolesAllowedAnnotation() throws Exception {
        getApplication()
                .getClasses().stream()
//                .filter(clazz -> clazz.getSimpleName().endsWith("Resource"))
                .flatMap(clazz -> Stream.of(clazz.getMethods()))
                .filter(method->method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class) || method.isAnnotationPresent(PUT.class) || method.isAnnotationPresent(DELETE.class) || method.isAnnotationPresent(PROPFIND.class))
                .filter(method -> !method.isAnnotationPresent(RolesAllowed.class))
                .forEach(method->fail("@RolesAllowed missing on "+method.getDeclaringClass().getSimpleName()+" method "+method.getName()));


    }

}

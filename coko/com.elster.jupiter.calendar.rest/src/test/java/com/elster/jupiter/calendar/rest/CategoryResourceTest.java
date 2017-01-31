/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.rest;

import com.elster.jupiter.calendar.Category;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class CategoryResourceTest extends CalendarApplicationTest {

    @Test
    public void testGetCategories() throws Exception {

        Category one = new FakeCategory(1, "one");
        Category two = new FakeCategory(2, "two");
        Category three = new FakeCategory(3, "three");

        when(calendarService.findAllCategories()).thenReturn(Arrays.asList(one, two, three));

        Response response = target("/categories").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());

        assertThat(jsonModel.<Integer>get("total")).isEqualTo(3);
        assertThat(jsonModel.<Integer>get("categories[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("categories[0].name")).isEqualTo("one");
        assertThat(jsonModel.<Integer>get("categories[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("categories[1].name")).isEqualTo("two");
        assertThat(jsonModel.<Integer>get("categories[2].id")).isEqualTo(3);
        assertThat(jsonModel.<String>get("categories[2].name")).isEqualTo("three");
    }


    private static class FakeCategory implements Category {
        private final long id;
        private final String name;

        FakeCategory(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public void save() {
        }

        @Override
        public String getDisplayName() {
            return name;
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}

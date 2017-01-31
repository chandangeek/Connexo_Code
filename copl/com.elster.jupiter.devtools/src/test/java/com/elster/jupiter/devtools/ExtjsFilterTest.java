/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools;

import java.net.URLDecoder;
import java.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ExtjsFilterTest {

    @Test
    public void testSimpleFilter() throws Exception {
        assertThat(URLDecoder.decode(ExtjsFilter.filter("id", "5"),"UTF-8")).isEqualTo("[{\"property\":\"id\",\"value\":\"5\"}]");
    }

    @Test
    public void testComplexFilter() throws Exception {
        assertThat(URLDecoder.decode(ExtjsFilter.filter().
                property("id", "5").
                property("available", "true").
                property("sort", "up").
                create(),"UTF-8")).
                isEqualTo("[{\"property\":\"id\",\"value\":\"5\"},{\"property\":\"available\",\"value\":\"true\"},{\"property\":\"sort\",\"value\":\"up\"}]");
    }

    @Test
    public void testComplexIntegerFilter() throws Exception {
        assertThat(URLDecoder.decode(ExtjsFilter.filter().
                property("ids", Arrays.asList(1,2,3,4,5)).
                create(),"UTF-8")).
                isEqualTo("[{\"property\":\"ids\",\"value\":[1,2,3,4,5]}]");
    }

    @Test
    public void testComplexLongFilter() throws Exception {
        assertThat(URLDecoder.decode(ExtjsFilter.filter().
                property("ids", Arrays.asList(1L,2L,3L,4L,5L)).
                create(),"UTF-8")).
                isEqualTo("[{\"property\":\"ids\",\"value\":[1,2,3,4,5]}]");
    }
}

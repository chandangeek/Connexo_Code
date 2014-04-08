package com.elster.jupiter.devtools;

import java.net.URLDecoder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ExtjsFilterTest {

    @Test
    public void testSimpleFilter() throws Exception {
        assertThat(URLDecoder.decode(ExtjsFilter.filter("id", "5"),"UTF-8")).isEqualTo("[{\"property\":\"id\",\"value\":\"5\"}]");
    }

    @Test
    public void testComplexFilter() throws Exception {
        assertThat(URLDecoder.decode(ExtjsFilter.complexFilter().
                addProperty("id", "5").
                addProperty("available", "true").
                addProperty("sort", "up").
                create(),"UTF-8")).
                isEqualTo("[{\"property\":\"id\",\"value\":\"5\"},{\"property\":\"available\",\"value\":\"true\"},{\"property\":\"sort\",\"value\":\"up\"}]");
    }
}

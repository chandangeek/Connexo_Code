/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.json.impl;

import com.elster.jupiter.util.json.JsonService;
import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonServiceImplTest {

    private JsonService jsonService = new JsonServiceImpl();

    @Test
    public void testProperties() {
        Properties props = new Properties();
        props.setProperty("id", "145");

        String serialized = jsonService.serialize(props);

        Properties deserialized = jsonService.deserialize(serialized, Properties.class);

        assertThat(deserialized).containsKey("id").containsValue("145").hasSize(1);
    }

    @Test
    public void testDummy() {
        Properties props = new Properties();
        props.setProperty("id", "145");

        String serialized = jsonService.serialize(new Dummy(props));

        Dummy deserialized = jsonService.deserialize(serialized, Dummy.class);

        assertThat(deserialized.getProperties()).containsKey("id").containsValue("145").hasSize(1);
    }

    @Test
    public void testDummyFromBytes() {
        Properties props = new Properties();
        props.setProperty("id", "145");

        String serialized = jsonService.serialize(new Dummy(props));

        Dummy deserialized = jsonService.deserialize(serialized.getBytes(), Dummy.class);

        assertThat(deserialized.getProperties()).containsKey("id").containsValue("145").hasSize(1);
    }

}

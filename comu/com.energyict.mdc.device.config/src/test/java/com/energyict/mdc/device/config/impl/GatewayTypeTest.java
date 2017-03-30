/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.GatewayType;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GatewayTypeTest{
    @Test
    public void testSearchWithNull(){
        assertThat(GatewayType.fromKey(null).isPresent()).isFalse();
    }

    @Test
    public void testSearchWithUnexisting(){
        assertThat(GatewayType.fromKey("some-string").isPresent()).isFalse();
    }

    @Test
    public void testFromKey(){
        Optional<GatewayType> typeOptional = GatewayType.fromKey("HAN");
        assertThat(typeOptional.isPresent()).isTrue();
        assertThat(typeOptional.get()).isEqualTo(GatewayType.HOME_AREA_NETWORK);
    }
}

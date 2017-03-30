/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link ComChannelPlaceHolder} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-20 (08:53)
 */
public class ComChannelPlaceHolderTest {

    @Test
    public void noComChannelAfterConstruction () {
        // Business method
        ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.empty();

        // Asserts
        assertThat(comChannelPlaceHolder.getComPortRelatedComChannel()).isNull();
    }

    @Test
    public void comChannelAfterConstructionWithKnowComChannel () {
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);

        // Business method
        ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.forKnownComChannel(comChannel);

        // Asserts
        assertThat(comChannelPlaceHolder.getComPortRelatedComChannel()).isEqualTo(comChannel);
    }

    @Test
    public void testSetter () {
        ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.empty();
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);

        // Business method
        comChannelPlaceHolder.setComPortRelatedComChannel(comChannel);

        // Asserts
        assertThat(comChannelPlaceHolder.getComPortRelatedComChannel()).isEqualTo(comChannel);
    }

}
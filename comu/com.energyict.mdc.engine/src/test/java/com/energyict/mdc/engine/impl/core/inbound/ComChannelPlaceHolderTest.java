package com.energyict.mdc.engine.impl.core.inbound;

import org.junit.*;

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
        assertThat(comChannelPlaceHolder.getComChannel()).isNull();
    }

    @Test
    public void comChannelAfterConstructionWithKnowComChannel () {
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);

        // Business method
        ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.forKnownComChannel(comChannel);

        // Asserts
        assertThat(comChannelPlaceHolder.getComChannel()).isEqualTo(comChannel);
    }

    @Test
    public void testSetter () {
        ComChannelPlaceHolder comChannelPlaceHolder = ComChannelPlaceHolder.empty();
        ComPortRelatedComChannel comChannel = mock(ComPortRelatedComChannel.class);

        // Business method
        comChannelPlaceHolder.setComChannel(comChannel);

        // Asserts
        assertThat(comChannelPlaceHolder.getComChannel()).isEqualTo(comChannel);
    }

}
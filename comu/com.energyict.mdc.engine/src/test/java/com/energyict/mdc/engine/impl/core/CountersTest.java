/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link Counters} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-26 (11:26)
 */
public class CountersTest {

    private static final int NUMBER_OF_BYTES_READ = 123;
    private static final int NUMBER_OF_BYTES_SENT = 321;

    @Test
    public void testVirginCounters () {
        // Business method
        Counters counters = new Counters();

        // Asserts
        assertThat(counters.getBytesRead()).isZero();
        assertThat(counters.getBytesSent()).isZero();
        assertThat(counters.getPacketsRead()).isZero();
        assertThat(counters.getPacketsSent()).isZero();
    }

    @Test
    public void testReadOnly () {
        Counters counters = new Counters();

        // Business method
        counters.bytesRead(NUMBER_OF_BYTES_READ);
        counters.packetRead();

        // Asserts
        assertThat(counters.getBytesRead()).isEqualTo(NUMBER_OF_BYTES_READ);
        assertThat(counters.getBytesSent()).isZero();
        assertThat(counters.getPacketsRead()).isEqualTo(1);
        assertThat(counters.getPacketsSent()).isZero();
    }

    @Test
    public void testSentOnly () {
        Counters counters = new Counters();

        // Business method
        counters.bytesSent(NUMBER_OF_BYTES_SENT);
        counters.packetSent();

        // Asserts
        assertThat(counters.getBytesSent()).isEqualTo(NUMBER_OF_BYTES_SENT);
        assertThat(counters.getBytesRead()).isZero();
        assertThat(counters.getPacketsSent()).isEqualTo(1);
        assertThat(counters.getPacketsRead()).isZero();
    }

    @Test
    public void testReadAndSent () {
        Counters counters = new Counters();

        // Business method
        counters.bytesRead(NUMBER_OF_BYTES_READ);
        counters.packetRead();
        counters.bytesSent(NUMBER_OF_BYTES_SENT);
        counters.packetSent();

        // Asserts
        assertThat(counters.getBytesRead()).isEqualTo(NUMBER_OF_BYTES_READ);
        assertThat(counters.getPacketsRead()).isEqualTo(1);
        assertThat(counters.getBytesSent()).isEqualTo(NUMBER_OF_BYTES_SENT);
        assertThat(counters.getPacketsSent()).isEqualTo(1);
    }

    @Test
    public void testResetRead () {
        Counters counters = new Counters();
        counters.bytesRead(NUMBER_OF_BYTES_READ);
        counters.packetRead();
        counters.bytesSent(NUMBER_OF_BYTES_SENT);
        counters.packetSent();

        // Business method
        counters.resetBytesRead();
        counters.resetPacketsRead();

        // Asserts
        assertThat(counters.getBytesRead()).isZero();
        assertThat(counters.getPacketsRead()).isZero();
        assertThat(counters.getBytesSent()).isEqualTo(NUMBER_OF_BYTES_SENT);
        assertThat(counters.getPacketsSent()).isEqualTo(1);
    }

    @Test
    public void testResetSent () {
        Counters counters = new Counters();
        counters.bytesRead(NUMBER_OF_BYTES_READ);
        counters.packetRead();
        counters.bytesSent(NUMBER_OF_BYTES_SENT);
        counters.packetSent();

        // Business method
        counters.resetBytesSent();
        counters.resetPacketsSent();

        // Asserts
        assertThat(counters.getBytesRead()).isEqualTo(NUMBER_OF_BYTES_READ);
        assertThat(counters.getPacketsRead()).isEqualTo(1);
        assertThat(counters.getBytesSent()).isZero();
        assertThat(counters.getPacketsSent()).isZero();
    }

}
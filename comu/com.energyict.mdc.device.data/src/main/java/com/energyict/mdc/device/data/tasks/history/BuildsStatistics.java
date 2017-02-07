/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface BuildsStatistics<S> {

    S addSentBytes(long numberOfBytes);

    S addReceivedBytes(long numberOfBytes);

    S addSentPackets(long numberOfPackets);

    S addReceivedPackets(long numberOfPackets);

    S resetSentBytes();

    S resetReceivedBytes();

    S resetSentPackets();

    S resetReceivedPackets();

    long getSentBytes();

    long getReceivedBytes();

    long getSentPackets();

    long getReceivedPackets();
}
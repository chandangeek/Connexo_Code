/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.monitor.InboundComPortOperationalStatistics;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link InboundComPortOperationalStatistics} interface.
 */
public class InboundComPortOperationalStatisticsImpl extends OperationalStatisticsImpl implements ServerInboundComPortOperationalStatistics {

    public static final String NUMBER_OF_CONNECTIONS = "numberOfConnections";
    private static final String NUMBER_OF_CONNECTIONS_DESCRIPTION = "Number of connections";
    public static final String LAST_CONNECTION_TIMESTAMP = "lastConnectionTimeStamp";
    private static final String LAST_CONNECTION_TIMESTAMP_DESCRIPTION = "Last Connection date";
    public static final String LAST_CONNECTION_DEVICE_MRID = "DeviceMRIDlastConnection";
    private static final String LAST_CONNECTION_DEVICE_MRID_DESCRIPTION = "MRID of Last device connected";

    private int numberOfConnections;
    private Optional<Date> lastConnectionTimeStamp = Optional.empty();
    private String lastConnectedDeviceMRID;

    public InboundComPortOperationalStatisticsImpl(Clock clock, Thesaurus thesaurus) {
        super(clock, thesaurus, null);
    }

    @Override
    public long getNumberOfConnections() {
        return numberOfConnections;
    }

    @Override
    public Optional<Date> getLastConnectionTimestamp() {
        return lastConnectionTimeStamp;
    }

    @Override
    public String getLastConnectionDeviceMRID() {
        return lastConnectedDeviceMRID;
    }

    @Override
    public void notifyConnection() {
        this.lastConnectionTimeStamp = Optional.of(new Date(getClock().millis()));
        numberOfConnections++;
    }

    @Override
    public void deviceRecognized(String deviceMRID) {
        this.lastConnectedDeviceMRID = deviceMRID;
    }

    protected void addItemNames (List<String> itemNames) {
        super.addItemNames(itemNames);
        itemNames.add(NUMBER_OF_CONNECTIONS);
        itemNames.add(LAST_CONNECTION_TIMESTAMP);
        itemNames.add(LAST_CONNECTION_DEVICE_MRID);
    }

    protected void addItemDescriptions (List<String> itemDescriptions) {
        itemDescriptions.add(NUMBER_OF_CONNECTIONS_DESCRIPTION);
        itemDescriptions.add(LAST_CONNECTION_TIMESTAMP_DESCRIPTION);
        itemDescriptions.add(LAST_CONNECTION_DEVICE_MRID_DESCRIPTION);
    }

    protected void addItemTypes (List<OpenType> itemTypes) {
        super.addItemTypes(itemTypes);
        itemTypes.add(SimpleType.INTEGER);
        itemTypes.add(SimpleType.DATE);
        itemTypes.add(SimpleType.STRING);
    }

    @Override
    protected void initializeAccessors (List<CompositeDataItemAccessor> accessors) {
        super.initializeAccessors(accessors);
        accessors.add(
                new CompositeDataItemAccessor(
                        NUMBER_OF_CONNECTIONS,
                        this::getNumberOfConnections));
        accessors.add(
                new CompositeDataItemAccessor(
                        LAST_CONNECTION_TIMESTAMP,
                        () -> getLastConnectionTimestamp().map(Date::toString).orElse("")));
        accessors.add(
                new CompositeDataItemAccessor(
                        LAST_CONNECTION_DEVICE_MRID,
                        this::getLastConnectionDeviceMRID));
    }
}
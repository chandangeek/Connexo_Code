/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import ch.iec.tc57._2011.masterdatalinkageconfig.EndDevice;
import ch.iec.tc57._2011.masterdatalinkageconfig.MasterDataLinkageConfig;
import ch.iec.tc57._2011.masterdatalinkageconfig.Meter;
import ch.iec.tc57._2011.masterdatalinkageconfig.Name;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigRequestMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;

import java.time.Instant;

public class MasterDataLinkageMessageBuilder {
    private String meterName;
    private String meterMRID;
    private String meterRole;
    private String usagePointName;
    private String usagePointMRID;
    private String endDeviceName;
    private String endDeviceMRID;
    private Instant createdDateTime;
    private Instant effectiveDateTime;
    private boolean eraseMeterList = false;
    private boolean eraseUsagePointList = false;
    private boolean eraseEndDeviceList = false;
    private boolean spawnLists = false;
    private boolean dropPayload = false;
    private boolean dropConfigEvent = false;
    private boolean asyncReplyFlag = false;

    private final ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory requestFactory = new ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory();
    private final ch.iec.tc57._2011.masterdatalinkageconfig.ObjectFactory contentFactory = new ch.iec.tc57._2011.masterdatalinkageconfig.ObjectFactory();

    private MasterDataLinkageMessageBuilder() {
    }

    static MasterDataLinkageMessageBuilder createEmptyMessage() {
        return new MasterDataLinkageMessageBuilder();
    }

    MasterDataLinkageMessageBuilder withMeterMRID(String meterMRID) {
        this.meterMRID = meterMRID;
        eraseMeterList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withAsyncReplyFlag(boolean asyncReplyFlag) {
        this.asyncReplyFlag = asyncReplyFlag;
        return this;
    }

    MasterDataLinkageMessageBuilder withMeterName(String meterName) {
        this.meterName = meterName;
        eraseMeterList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withMeterRole(String meterRole) {
        this.meterRole = meterRole;
        eraseMeterList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withUsagePointMRID(String usagePointMRID) {
        this.usagePointMRID = usagePointMRID;
        eraseUsagePointList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withUsagePointName(String usagePointName) {
        this.usagePointName = usagePointName;
        eraseUsagePointList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withEndDeviceMRID(String endDeviceMRID) {
        this.endDeviceMRID = endDeviceMRID;
        eraseEndDeviceList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withEndDeviceName(String endDeviceName) {
        this.endDeviceName = endDeviceName;
        eraseEndDeviceList = false;
        return this;
    }

    MasterDataLinkageMessageBuilder withCreatedDateTime(Instant createdDateTime) {
        this.createdDateTime = createdDateTime;
        return this;
    }

    MasterDataLinkageMessageBuilder withEffectiveDateTime(Instant effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
        return this;
    }

    MasterDataLinkageMessageBuilder eraseMeterList() {
        eraseMeterList = true;
        return this;
    }

    MasterDataLinkageMessageBuilder eraseUsagePointList() {
        eraseUsagePointList = true;
        return this;
    }

    MasterDataLinkageMessageBuilder eraseEndDeviceList() {
        eraseEndDeviceList = true;
        return this;
    }

    MasterDataLinkageMessageBuilder spawnLists() {
        spawnLists = true;
        return this;
    }

    MasterDataLinkageMessageBuilder dropPayload() {
        dropPayload = true;
        return this;
    }

    MasterDataLinkageMessageBuilder dropConfigEvent() {
        dropConfigEvent = true;
        return this;
    }

    MasterDataLinkageConfigRequestMessageType build() {
        if (dropPayload) {
            return requestFactory.createMasterDataLinkageConfigRequestMessageType();
        }

        MasterDataLinkageConfig configNode = contentFactory.createMasterDataLinkageConfig();

        if (!eraseMeterList) {
            Meter meterNode = contentFactory.createMeter();
            meterNode.setMRID(meterMRID);
            meterNode.setRole(meterRole);
            if (meterName != null) {
                Name meterNameNode = contentFactory.createName();
                meterNameNode.setName(meterName);
                meterNode.getNames().add(meterNameNode);
            }
            configNode.getMeter().add(meterNode);
        }

        if (!eraseUsagePointList) {
            UsagePoint usagePointNode = contentFactory.createUsagePoint();
            usagePointNode.setMRID(usagePointMRID);
            if (usagePointName != null) {
                Name usagePointNameNode = contentFactory.createName();
                usagePointNameNode.setName(usagePointName);
                usagePointNode.getNames().add(usagePointNameNode);
            }
            configNode.getUsagePoint().add(usagePointNode);
        }

        if (!eraseEndDeviceList) {
            EndDevice endDeviceNode = contentFactory.createEndDevice();
            endDeviceNode.setMRID(endDeviceMRID);
            if (endDeviceName != null) {
                Name endDeviceNameNode = contentFactory.createName();
                endDeviceNameNode.setName(endDeviceName);
                endDeviceNode.getNames().add(endDeviceNameNode);
            }
            configNode.getEndDevice().add(endDeviceNode);
        }

        if (spawnLists && !eraseUsagePointList) {
            configNode.getUsagePoint().add(configNode.getUsagePoint().get(0));
        }
        if (spawnLists && !eraseEndDeviceList) {
            configNode.getEndDevice().add(configNode.getEndDevice().get(0));
        }
        if (spawnLists && !eraseMeterList) {
            configNode.getMeter().add(configNode.getMeter().get(0));
        }
        if (!dropConfigEvent) {
            ConfigurationEvent configurationEvent = contentFactory.createConfigurationEvent();
            configurationEvent.setCreatedDateTime(createdDateTime);
            configurationEvent.setEffectiveDateTime(effectiveDateTime);
            configNode.setConfigurationEvent(configurationEvent);
        }

        MasterDataLinkageConfigRequestMessageType message = requestFactory
                .createMasterDataLinkageConfigRequestMessageType();
        message.setPayload(requestFactory.createMasterDataLinkageConfigPayloadType());
        message.getPayload().setMasterDataLinkageConfig(configNode);

        message.setHeader(new HeaderType());
        message.getHeader().setAsyncReplyFlag(asyncReplyFlag);
        return message;
    }
}

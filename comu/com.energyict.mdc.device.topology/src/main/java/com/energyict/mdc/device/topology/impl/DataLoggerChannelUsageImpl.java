package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.ImplField;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2016
 * Time: 11:15
 */
public class DataLoggerChannelUsageImpl implements DataLoggerChannelUsage {

    public enum Field implements ImplField {
        ORIGIN("dataloggerReference"),       // holding the primary key of its parent's DataLoggerReferenceImpl
        STARTTIME("startTime"),             // holding the primary key of its parent's DataLoggerReferenceImpl
        ORIGIN_CHANNEL("slaveChannel"),
        GATEWAY_CHANNEL("dataLoggerChannel")
        ;

        private final String javaFieldName;

        Field(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        @Override
        public String fieldName() {
            return javaFieldName;
        }
    }


    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private TemporalReference<DataLoggerReferenceImpl> dataloggerReference = Temporals.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Channel> dataLoggerChannel = ValueReference.absent();
    @IsPresent(groups = { Save.Create.class, Save.Update.class }, message = "{" + MessageSeeds.Keys.VALUE_IS_REQUIRED_KEY + "}")
    private Reference<Channel> slaveChannel = ValueReference.absent();

    private long startTime;

    public DataLoggerChannelUsageImpl createFor(DataLoggerReferenceImpl dataLoggerReference, Channel slaveChannel, Channel dataLoggerChannel ) {
        this.dataloggerReference.add(dataLoggerReference);
        this.slaveChannel.set(slaveChannel);
        this.dataLoggerChannel.set(dataLoggerChannel);
        return this;
    }

    @Override
    public DataLoggerReferenceImpl getDataLoggerReference() {
        //TODO cleanup
        return this.dataloggerReference.effective(Instant.now()).get();
    }

    @Override
    public Channel getDataLoggerChannel() {
        return dataLoggerChannel.get();
    }

    @Override
    public Channel getSlaveChannel() {
        return slaveChannel.get();
    }
}

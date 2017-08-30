package com.energyict.mdc.device.topology.rest.info;

import com.energyict.mdc.device.data.Device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * NodeInfo for Device
 * Copyrights EnergyICT
 * Date: 6/01/2017
 * Time: 12:35
 */
@JsonIgnoreType
@JsonPropertyOrder({ "id", "gateway"})
public class DeviceNodeInfo extends NodeInfo<Device>{

    public DeviceNodeInfo(Device device, Optional<Device> parent, Optional<Range<Instant>> period) {
        super(device);
        if (parent.isPresent()) {
            setParent(parent.get());
        }
        if (period.isPresent()){
            setPeriod(period.get());
        }
    }

    @SuppressWarnings("unused")
    @JsonIgnore
    public PeriodInfo getPeriod(){
        return (PeriodInfo) allProperties.get("period");
    }

    @Override
    @JsonIgnore
    public Class getObjectClass() {
        return Device.class;
    }
    @JsonIgnore
    public Device getDevice(){
        return super.getNodeObject();
    }

    private void setPeriod(Range<Instant> period){
        if (period != null) {
            allProperties.put("period", new PeriodInfo(period));
        }else {
            allProperties.remove("period");
        }
    }

    @JsonTypeName("period")
    public static class PeriodInfo {
        @JsonProperty
        private Long start;
        @JsonProperty
        private Long end;

        public PeriodInfo(Range<Instant> period){
            if (period.hasLowerBound()) {
                try {
                    this.start = period.lowerEndpoint().toEpochMilli();
                } catch (java.lang.ArithmeticException e) {
                    this.start = null;
                }
            }
            if (period.hasUpperBound() && period.upperBoundType() == BoundType.CLOSED ) {
                try {
                    this.end = period.upperEndpoint().toEpochMilli();
                }catch(java.lang.ArithmeticException e){
                    this.end = null;
                }
            }
        }
        // for serialization purposes
        public PeriodInfo(){};

        public Long getStart() {
            return start;
        }

        public Long getEnd() {
            return end;
        }
    }

}

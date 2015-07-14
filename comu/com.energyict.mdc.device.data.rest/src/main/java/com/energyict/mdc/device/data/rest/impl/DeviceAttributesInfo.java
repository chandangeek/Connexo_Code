package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceAttributesInfo {
    public enum DeviceAttribute {
        MRID {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Collections.emptyList();
            }
        },
        DEVICE_TYPE {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Collections.emptyList();
            }
        },
        DEVICE_CONFIGURATION {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Collections.emptyList();
            }
        },
        SERIAL_NUMBER,
        YEAR_OF_CERTIFICATION,
        LIFE_CYCLE_STATE {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Collections.emptyList();
            }
        },
        BATCH,
        USAGE_POINT,
        SERVICE_CATEGORY {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return USAGE_POINT.attributeIsEditableForStates();
            }

            @Override
            public List<DefaultState> attributeIsAllowedForStates() {
                return USAGE_POINT.attributeIsAllowedForStates();
            }
        },
        SHIPMENT_DATE {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Arrays.asList(DefaultState.IN_STOCK);
            }
        },
        INSTALLATION_DATE {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Arrays.asList(
                        DefaultState.COMMISSIONING,
                        DefaultState.ACTIVE,
                        DefaultState.INACTIVE
                );
            }

            @Override
            public List<DefaultState> attributeIsAllowedForStates() {
                return Arrays.asList(
                        DefaultState.COMMISSIONING,
                        DefaultState.ACTIVE,
                        DefaultState.INACTIVE,
                        DefaultState.DECOMMISSIONED
                );
            }
        },
        DEACTIVATION_DATE {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Arrays.asList(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED);
            }

            @Override
            public List<DefaultState> attributeIsAllowedForStates() {
                return Arrays.asList(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED);
            }
        },
        DECOMMISSIONING_DATE {
            @Override
            public List<DefaultState> attributeIsEditableForStates() {
                return Arrays.asList(DefaultState.DECOMMISSIONED);
            }

            @Override
            public List<DefaultState> attributeIsAllowedForStates() {
                return Arrays.asList(DefaultState.DECOMMISSIONED);
            }
        },;

        public List<DefaultState> attributeIsEditableForStates() {
            return Arrays.asList(
                    DefaultState.IN_STOCK,
                    DefaultState.COMMISSIONING,
                    DefaultState.ACTIVE,
                    DefaultState.INACTIVE
            );
        }

        public List<DefaultState> attributeIsAllowedForStates() {
            return null;
        }

        public boolean isAvailableForState(State state) {
            if (this.attributeIsAllowedForStates() != null) {
                Optional<DefaultState> correspondingDefaultState = DefaultState.from(state);
                return correspondingDefaultState.isPresent()
                        && this.attributeIsAllowedForStates().contains(correspondingDefaultState.get());
            }
            return true;
        }

        public boolean isEditableForState(State state) {
            if (this.attributeIsEditableForStates() != null) {
                Optional<DefaultState> correspondingDefaultState = DefaultState.from(state);
                return correspondingDefaultState.isPresent()
                        && this.attributeIsEditableForStates().contains(correspondingDefaultState.get());
            }
            return false;
        }
    }

    public DeviceAttributeInfo<String> mrid;
    public DeviceAttributeInfo<String> deviceType;
    public DeviceAttributeInfo<String> deviceConfiguration;
    public DeviceAttributeInfo<String> serialNumber;
    public DeviceAttributeInfo<Integer> yearOfCertification;
    public DeviceAttributeInfo<String> lifeCycleState;
    public DeviceAttributeInfo<String> batch;
    public DeviceAttributeInfo<String> usagePoint;
    public DeviceAttributeInfo<Instant> shipmentDate;
    public DeviceAttributeInfo<Instant> installationDate;
    public DeviceAttributeInfo<Instant> deactivationDate;
    public DeviceAttributeInfo<Instant> decommissioningDate;
    public long deviceVersion;

    @JsonIgnore
    public Optional<Instant> getShipmentDate(){
        if (this.shipmentDate == null || this.shipmentDate.displayValue == null){
            return Optional.empty();
        }
        return Optional.of(this.shipmentDate.displayValue);
    }

    @JsonIgnore
    public Optional<Instant> getInstallationDate(){
        if (this.installationDate == null || this.installationDate.displayValue == null){
            return Optional.empty();
        }
        return Optional.of(this.installationDate.displayValue);
    }

    @JsonIgnore
    public Optional<Instant> getDeactivationDate(){
        if (this.deactivationDate == null || this.deactivationDate.displayValue == null){
            return Optional.empty();
        }
        return Optional.of(this.deactivationDate.displayValue);
    }

    @JsonIgnore
    public Optional<Instant> getDecommissioningDate(){
        if (this.decommissioningDate == null || this.decommissioningDate.displayValue == null){
            return Optional.empty();
        }
        return Optional.of(this.decommissioningDate.displayValue);
    }
}
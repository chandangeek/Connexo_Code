/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@ProviderType
public interface FirmwareManagementOptions extends FirmwareCheckManagementOptions {

    void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions);

    Set<ProtocolSupportedFirmwareOptions> getOptions();

    long getVersion();

    FirmwareManagementOptions EMPTY  = new FirmwareManagementOptions() {

        @Override
        public void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deactivate(FirmwareCheckManagementOption checkManagementOption) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isActivated(FirmwareCheckManagementOption checkManagementOption) {
            return false;
        }

        @Override
        public EnumSet<FirmwareStatus> getStatuses(FirmwareCheckManagementOption checkManagementOption) {
            return EnumSet.noneOf(FirmwareStatus.class);
        }

        @Override
        public void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<ProtocolSupportedFirmwareOptions> getOptions() {
            return Collections.emptySet();
        }

        @Override
        public long getVersion() {
            return 0;
        }
    };
}

package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.security.InheritedAuthenticationDeviceAccessLevel;
import com.energyict.protocolimplv2.security.InheritedEncryptionDeviceAccessLevel;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Place holder protocol, requests are handled and parsed in the master protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 4/12/12
 * Time: 13:44
 * Author: khe
 */
public class ACE4000MBus extends ACE4000Outbound {

    private final Provider<InheritedEncryptionDeviceAccessLevel> inheritedEncryptionDeviceAccessLevelProvider;
    private final Provider<InheritedAuthenticationDeviceAccessLevel> inheritedAuthenticationDeviceAccessLevelProvider;

    @Inject
    public ACE4000MBus(Clock clock, PropertySpecService propertySpecService, IssueService issueService,
                       MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService,
                       CollectedDataFactory collectedDataFactory, MeteringService meteringService, Thesaurus thesaurus,
                       Provider<InheritedEncryptionDeviceAccessLevel> inheritedEncryptionDeviceAccessLevelProvider,
                       Provider<InheritedAuthenticationDeviceAccessLevel> inheritedAuthenticationDeviceAccessLevelProvider) {
        super(clock, propertySpecService, issueService, readingTypeUtilService, identificationService,
                collectedDataFactory, meteringService, thesaurus);
        this.inheritedEncryptionDeviceAccessLevelProvider = inheritedEncryptionDeviceAccessLevelProvider;
        this.inheritedAuthenticationDeviceAccessLevelProvider = inheritedAuthenticationDeviceAccessLevelProvider;
    }

    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        List<DeviceProtocolCapabilities> capabilities = new ArrayList<>();
        capabilities.add(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
        return capabilities;
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        List<EncryptionDeviceAccessLevel> encryptionAccessLevels = new ArrayList<>();
        encryptionAccessLevels.addAll(super.getEncryptionAccessLevels());
        encryptionAccessLevels.add(inheritedEncryptionDeviceAccessLevelProvider.get());
        return encryptionAccessLevels;
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        List<AuthenticationDeviceAccessLevel> authenticationAccessLevels = new ArrayList<>();
        authenticationAccessLevels.addAll(super.getAuthenticationAccessLevels());
        authenticationAccessLevels.add(inheritedAuthenticationDeviceAccessLevelProvider.get());
        return authenticationAccessLevels;
    }

    @Override
    public String getProtocolDescription() {
        return "Actaris ACE4000 MeterXML Mbus Device";
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}
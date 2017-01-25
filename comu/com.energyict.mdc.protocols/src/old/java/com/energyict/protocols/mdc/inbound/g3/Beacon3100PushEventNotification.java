package com.energyict.protocols.mdc.inbound.g3;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Does pretty much the same as the PushEventNotification of the G3 gateway,
 * but uses the Beacon3100 protocol to connect to the DC device.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/06/2015 - 11:33
 */
public class Beacon3100PushEventNotification extends PushEventNotification {

    //TODO junit test with encrypted traces

    /**
     * The obiscode of the logbook to store the received events in
     * Note that this one (Beacon main logbook) is different from the G3 gateway main logbook.
     */
    private static final ObisCode OBIS_STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    private static final String PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY = "ProvideProtocolJavaClassName";
    private final IdentificationService identificationService;
    private final CollectedDataFactory collectedDataFactory;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final SocketService socketService;
    private final SerialComponentService serialComponentService;
    private final IssueService issueService;
    private final TopologyService topologyService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final MeteringService meteringService;
    private final LoadProfileFactory loadProfileFactory;
    private final Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider;
    private boolean provideProtocolJavaClasName = true;

    @Inject
    public Beacon3100PushEventNotification(IdentificationService identificationService, CollectedDataFactory collectedDataFactory, PropertySpecService propertySpecService, Thesaurus thesaurus, Clock clock, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, MeteringService meteringService, LoadProfileFactory loadProfileFactory, Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(identificationService, collectedDataFactory, propertySpecService, thesaurus, meteringService, dsmrSecuritySupportProvider, socketService, issueService);
        this.identificationService = identificationService;
        this.collectedDataFactory = collectedDataFactory;
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.socketService = socketService;
        this.serialComponentService = serialComponentService;
        this.issueService = issueService;
        this.topologyService = topologyService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.meteringService = meteringService;
        this.loadProfileFactory = loadProfileFactory;
        this.dsmrSecuritySupportProvider = dsmrSecuritySupportProvider;
    }

    protected BeaconPSKProvider getPskProvider() {
        return BeaconPSKProviderFactory.getInstance(provideProtocolJavaClasName)
                .getPSKProvider(getDeviceIdentifier(), getContext(), clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory, dsmrSecuritySupportProvider);
    }

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new EventPushNotificationParser(comChannel, getContext(), OBIS_STANDARD_EVENT_LOG, identificationService, collectedDataFactory, thesaurus, propertySpecService, meteringService);
        }
        return parser;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        final List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(propertySpecService.booleanSpec().named(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY).describedAs("").finish());
        return optionalProperties;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-04-25 11:28:57 +0200 (Mon, 25 Apr 2016)$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        super.addProperties(properties);
        this.provideProtocolJavaClasName = properties.<Boolean>getTypedProperty(PROVIDE_PROTOCOL_JAVA_CLASS_NAME_PROPERTY, true);
    }
}
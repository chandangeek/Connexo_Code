package com.energyict.mdc.protocol.api.cim;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Factory containing static methods for the easy creation of different {@link EndDeviceEventType}s without knowing the exact code.
 *
 * @author sva
 * @since 4/06/13 - 15:42
 */
@Component(name="com.energyict.mdc.protocols.api.cim.enddeviceeventtype.factory", service = {EndDeviceEventTypeFactory.class}, property = "name=CEF", immediate = true)
public class EndDeviceEventTypeFactory {

    private static final Logger LOGGER = Logger.getLogger(EndDeviceEventTypeFactory.class.getName());
    static AtomicReference<EndDeviceEventTypeFactory> current = new AtomicReference<>();

    private volatile MeteringService meteringService;

    public EndDeviceEventTypeFactory() {
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate() {
        current.set(this);
    }

    @Deactivate
    public void deactivate() {
        current.set(null);
    }

    public EndDeviceEventType getEndDeviceEventType(String mRID) {
        for (EndDeviceEventType endDeviceEventType : this.meteringService.getAvailableEndDeviceEventTypes()) {
            if (endDeviceEventType.getMRID().equals(mRID)) {
                return endDeviceEventType;
            }
        }
        LOGGER.severe("EndDeviceEventType missing: " + mRID);
        return null;
    }
}
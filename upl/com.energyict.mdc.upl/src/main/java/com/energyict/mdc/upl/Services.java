package com.energyict.mdc.upl;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.SecurityService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Groups all the services that universal protocols will be needing.
 * As the platform starts up, it will want/need to provide implementations
 * for the services by calling the corresponding setter method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-21 (09:29)
 */
public class Services {

    private static AtomicReference<PropertySpecService> PROPERTY_SPEC_SERVICE = new AtomicReference<>();
    private static AtomicReference<NlsService> NLS_SERVICE = new AtomicReference<>();
    private static AtomicReference<SecurityService> SECURITY_SERVICE = new AtomicReference<>();
    private static AtomicReference<Converter> CONVERTER = new AtomicReference<>();
    private static AtomicReference<Extractor> EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<TariffCalendarFinder> TARIFF_CALENDAR_FINDER = new AtomicReference<>();
    private static AtomicReference<DeviceMessageFileFinder> DEVICE_MESSAGE_FINDER = new AtomicReference<>();
    private static AtomicReference<CollectedDataFactory> COLLECTED_DATA_FACTORY = new AtomicReference<>();
    private static AtomicReference<IssueFactory> ISSUE_FACTORY = new AtomicReference<>();
    private static AtomicReference<DateFormatter> DATE_FORMATTER = new AtomicReference<>();

    public static Object serviceOfType(Class serviceType) {
        if (PropertySpecService.class.equals(serviceType)) {
            return propertySpecService();
        } else if (NlsService.class.equals(serviceType)) {
            return nlsService();
        } else if (SecurityService.class.equals(serviceType)) {
            return securityService();
        } else if (Converter.class.equals(serviceType)) {
            return converter();
        } else if (Extractor.class.equals(serviceType)) {
            return extractor();
        } else if (TariffCalendarFinder.class.equals(serviceType)) {
            return tariffCalendarFinder();
        } else if (DeviceMessageFileFinder.class.equals(serviceType)) {
            return deviceMessageFileFinder();
        } else if (CollectedDataFactory.class.equals(serviceType)) {
            return collectedDataFactory();
        } else if (IssueFactory.class.equals(serviceType)) {
            return issueFactory();
        } else if (DateFormatter.class.equals(serviceType)) {
            return dateFormatter();
        } else {
            throw new UnknownServiceType(serviceType);
        }
    }

    public static PropertySpecService propertySpecService() {
        return PROPERTY_SPEC_SERVICE.get();
    }

    public static void propertySpecService(PropertySpecService propertySpecService) {
        PROPERTY_SPEC_SERVICE.set(propertySpecService);
    }

    public static NlsService nlsService() {
        return NLS_SERVICE.get();
    }

    public static void nlsService(NlsService nlsService) {
        NLS_SERVICE.set(nlsService);
    }

    public static SecurityService securityService() {
        return SECURITY_SERVICE.get();
    }

    public static void securityService(SecurityService securityService) {
        SECURITY_SERVICE.set(securityService);
    }

    public static Converter converter() {
        return CONVERTER.get();
    }

    public static void converter(Converter converter) {
        CONVERTER.set(converter);
    }

    public static Extractor extractor() {
        return EXTRACTOR.get();
    }

    public static void extractor(Extractor extractor) {
        EXTRACTOR.set(extractor);
    }

    public static TariffCalendarFinder tariffCalendarFinder() {
        return TARIFF_CALENDAR_FINDER.get();
    }

    public static void tariffCalendarFinder(TariffCalendarFinder finder) {
        TARIFF_CALENDAR_FINDER.set(finder);
    }

    public static DeviceMessageFileFinder deviceMessageFileFinder() {
        return DEVICE_MESSAGE_FINDER.get();
    }

    public static void deviceMessageFileFinder(DeviceMessageFileFinder finder) {
        DEVICE_MESSAGE_FINDER.set(finder);
    }

    public static CollectedDataFactory collectedDataFactory() {
        return COLLECTED_DATA_FACTORY.get();
    }

    public static void collectedDataFactory(CollectedDataFactory collectedDataFactory) {
        COLLECTED_DATA_FACTORY.set(collectedDataFactory);
    }

    public static IssueFactory issueFactory() {
        return ISSUE_FACTORY.get();
    }

    public static void issueFactory(IssueFactory issueFactory) {
        ISSUE_FACTORY.set(issueFactory);
    }

    public static DateFormatter dateFormatter() {
        return DATE_FORMATTER.get();
    }

    public static void dateFormatter(DateFormatter dateFormatter) {
        DATE_FORMATTER.set(dateFormatter);
    }

    /**
     * Models the exceptional situation that occurs
     * when a service is requested that is not published
     * by the Services class.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2016-12-15 (09:42)
     */
    public static class UnknownServiceType extends RuntimeException {
        public UnknownServiceType(Class serviceType) {
            super("Unknown service type " + serviceType.getName());
        }
    }

}
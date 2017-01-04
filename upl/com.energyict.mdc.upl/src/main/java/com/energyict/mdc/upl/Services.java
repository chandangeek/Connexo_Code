package com.energyict.mdc.upl;

import com.energyict.mdc.upl.crypto.MD5Cryptographer;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.DateFormatter;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceGroupExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.RegisterExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
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
    private static AtomicReference<DeviceExtractor> DEVICE_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<DeviceGroupExtractor> DEVICE_GROUP_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<RegisterExtractor> REGISTER_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<LoadProfileExtractor> LOAD_PROFILE_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<NumberLookupExtractor> NUMBER_LOOKUP_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<DeviceMessageFileExtractor> DEVICE_MESSAGE_FILE_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<TariffCalendarExtractor> TARIFF_CALENDAR_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<TariffCalendarFinder> TARIFF_CALENDAR_FINDER = new AtomicReference<>();
    private static AtomicReference<DeviceMessageFileFinder> DEVICE_MESSAGE_FINDER = new AtomicReference<>();
    private static AtomicReference<CollectedDataFactory> COLLECTED_DATA_FACTORY = new AtomicReference<>();
    private static AtomicReference<IssueFactory> ISSUE_FACTORY = new AtomicReference<>();
    private static AtomicReference<DateFormatter> DATE_FORMATTER = new AtomicReference<>();
    private static AtomicReference<MD5Cryptographer> MD5_CRYPTOGRAPHER = new AtomicReference<>();

    public static Object serviceOfType(Class serviceType) {
        if (PropertySpecService.class.equals(serviceType)) {
            return propertySpecService();
        } else if (NlsService.class.equals(serviceType)) {
            return nlsService();
        } else if (SecurityService.class.equals(serviceType)) {
            return securityService();
        } else if (Converter.class.equals(serviceType)) {
            return converter();
        } else if (NumberLookupExtractor.class.equals(serviceType)) {
            return numberLookupExtractor();
        } else if (LoadProfileExtractor.class.equals(serviceType)) {
            return loadProfileExtractor();
        } else if (DeviceExtractor.class.equals(serviceType)) {
            return deviceExtractor();
        } else if (DeviceGroupExtractor.class.equals(serviceType)) {
            return deviceGroupExtractor();
        } else if (DeviceMessageFileExtractor.class.equals(serviceType)) {
            return deviceMessageFileExtractor();
        } else if (TariffCalendarExtractor.class.equals(serviceType)) {
            return tariffCalendarExtractor();
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
        } else if (MD5Cryptographer.class.equals(serviceType)) {
            return md5Cryptographer();
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

    public static RegisterExtractor registerExtractor() {
        return REGISTER_EXTRACTOR.get();
    }

    public static void registerExtractor(RegisterExtractor extractor) {
        REGISTER_EXTRACTOR.set(extractor);
    }

    public static LoadProfileExtractor loadProfileExtractor() {
        return LOAD_PROFILE_EXTRACTOR.get();
    }

    public static void loadProfileExtractor(LoadProfileExtractor extractor) {
        LOAD_PROFILE_EXTRACTOR.set(extractor);
    }

    public static NumberLookupExtractor numberLookupExtractor() {
        return NUMBER_LOOKUP_EXTRACTOR.get();
    }

    public static void numberLookupExtractor(NumberLookupExtractor extractor) {
        NUMBER_LOOKUP_EXTRACTOR.set(extractor);
    }

    public static DeviceExtractor deviceExtractor() {
        return DEVICE_EXTRACTOR.get();
    }

    public static void deviceExtractor(DeviceExtractor extractor) {
        DEVICE_EXTRACTOR.set(extractor);
    }

    public static DeviceGroupExtractor deviceGroupExtractor() {
        return DEVICE_GROUP_EXTRACTOR.get();
    }

    public static void deviceGroupExtractor(DeviceGroupExtractor extractor) {
        DEVICE_GROUP_EXTRACTOR.set(extractor);
    }

    public static DeviceMessageFileExtractor deviceMessageFileExtractor() {
        return DEVICE_MESSAGE_FILE_EXTRACTOR.get();
    }

    public static void deviceMessageFileExtractor(DeviceMessageFileExtractor extractor) {
        DEVICE_MESSAGE_FILE_EXTRACTOR.set(extractor);
    }

    public static TariffCalendarExtractor tariffCalendarExtractor() {
        return TARIFF_CALENDAR_EXTRACTOR.get();
    }

    public static void tariffCalendarExtractor(TariffCalendarExtractor extractor) {
        TARIFF_CALENDAR_EXTRACTOR.set(extractor);
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

    public static MD5Cryptographer md5Cryptographer() {
        return MD5_CRYPTOGRAPHER.get();
    }

    public static void md5Cryptographer(MD5Cryptographer md5Cryptographer) {
        MD5_CRYPTOGRAPHER.set(md5Cryptographer);
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
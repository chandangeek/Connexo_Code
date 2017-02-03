package com.energyict.mdc.upl;

import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.io.UPLSocketService;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateAliasFinder;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
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

    private static AtomicReference<RuntimeEnvironment> RUNTIME_ENVIRONMENT = new AtomicReference<>();
    private static AtomicReference<ObjectMapperService> OBJECT_MAPPER_SERVICE = new AtomicReference<>();
    private static AtomicReference<PropertySpecService> PROPERTY_SPEC_SERVICE = new AtomicReference<>();
    private static AtomicReference<NlsService> NLS_SERVICE = new AtomicReference<>();
    private static AtomicReference<SecurityService> SECURITY_SERVICE = new AtomicReference<>();
    private static AtomicReference<UPLSocketService> SOCKET_SERVICE = new AtomicReference<>();
    private static AtomicReference<Converter> CONVERTER = new AtomicReference<>();
    private static AtomicReference<DeviceMasterDataExtractor> DEVICE_MASTER_DATA_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<DeviceExtractor> DEVICE_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<DeviceGroupExtractor> DEVICE_GROUP_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<RegisterExtractor> REGISTER_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<LoadProfileExtractor> LOAD_PROFILE_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<NumberLookupExtractor> NUMBER_LOOKUP_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<NumberLookupFinder> NUMBER_LOOKUP_FINDER = new AtomicReference<>();
    private static AtomicReference<DeviceMessageFileExtractor> DEVICE_MESSAGE_FILE_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<TariffCalendarExtractor> TARIFF_CALENDAR_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<TariffCalendarFinder> TARIFF_CALENDAR_FINDER = new AtomicReference<>();
    private static AtomicReference<DeviceMessageFileFinder> DEVICE_MESSAGE_FINDER = new AtomicReference<>();
    private static AtomicReference<CollectedDataFactory> COLLECTED_DATA_FACTORY = new AtomicReference<>();
    private static AtomicReference<IssueFactory> ISSUE_FACTORY = new AtomicReference<>();
    private static AtomicReference<Formatter> FORMATTER = new AtomicReference<>();
    private static AtomicReference<X509Service> X509 = new AtomicReference<>();
    private static AtomicReference<KeyStoreService> KEY_STORE_SERVICE = new AtomicReference<>();
    private static AtomicReference<CertificateWrapperExtractor> CERTIFICATE_WRAPPER_EXTRACTOR = new AtomicReference<>();
    private static AtomicReference<CertificateAliasFinder> CERTIFICATE_ALIAS_FINDER = new AtomicReference<>();

    public static Object serviceOfType(Class serviceType) {
        if (PropertySpecService.class.equals(serviceType)) {
            return propertySpecService();
        } else if (RuntimeEnvironment.class.equals(serviceType)) {
            return runtimeEnvironment();
        } else if (ObjectMapperService.class.equals(serviceType)) {
            return objectMapperService();
        } else if (NlsService.class.equals(serviceType)) {
            return nlsService();
        } else if (SecurityService.class.equals(serviceType)) {
            return securityService();
        } else if (UPLSocketService.class.equals(serviceType)) {
            return socketService();
        } else if (Converter.class.equals(serviceType)) {
            return converter();
        } else if (NumberLookupExtractor.class.equals(serviceType)) {
            return numberLookupExtractor();
        } else if (NumberLookupFinder.class.equals(serviceType)) {
            return numberLookupFinder();
        } else if (CertificateWrapperExtractor.class.equals(serviceType)) {
            return certificateWrapperExtractor();
        } else if (CertificateAliasFinder.class.equals(serviceType)) {
            return certificateAliasFinder();
        } else if (LoadProfileExtractor.class.equals(serviceType)) {
            return loadProfileExtractor();
        } else if (DeviceMasterDataExtractor.class.equals(serviceType)) {
            return deviceMasterDataExtractor();
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
        } else if (Formatter.class.equals(serviceType)) {
            return formatter();
        } else if (X509Service.class.equals(serviceType)) {
            return x509Service();
        } else if (KeyStoreService.class.equals(serviceType)) {
            return keyStoreService();
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

    public static RuntimeEnvironment runtimeEnvironment() {
        return RUNTIME_ENVIRONMENT.get();
    }

    public static void runtimeEnvironment(RuntimeEnvironment runtimeEnvironment) {
        RUNTIME_ENVIRONMENT.set(runtimeEnvironment);
    }

    public static ObjectMapperService objectMapperService() {
        return OBJECT_MAPPER_SERVICE.get();
    }

    public static void objectMapperService(ObjectMapperService objectMapperService) {
        OBJECT_MAPPER_SERVICE.set(objectMapperService);
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

    public static UPLSocketService socketService() {
        return SOCKET_SERVICE.get();
    }

    public static void socketService(UPLSocketService socketService) {
        SOCKET_SERVICE.set(socketService);
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

    public static NumberLookupFinder numberLookupFinder() {
        return NUMBER_LOOKUP_FINDER.get();
    }

    public static void numberLookupFinder(NumberLookupFinder extractor) {
        NUMBER_LOOKUP_FINDER.set(extractor);
    }

    public static CertificateWrapperExtractor certificateWrapperExtractor() {
        return CERTIFICATE_WRAPPER_EXTRACTOR.get();
    }

    public static void certificateWrapperExtractor(CertificateWrapperExtractor certificateWrapperExtractor) {
        CERTIFICATE_WRAPPER_EXTRACTOR.set(certificateWrapperExtractor);
    }

    public static CertificateAliasFinder certificateAliasFinder() {
        return CERTIFICATE_ALIAS_FINDER.get();
    }

    public static void certificateAliasFinder(CertificateAliasFinder certificateAliasFinder) {
        CERTIFICATE_ALIAS_FINDER.set(certificateAliasFinder);
    }

    public static DeviceMasterDataExtractor deviceMasterDataExtractor() {
        return DEVICE_MASTER_DATA_EXTRACTOR.get();
    }

    public static void deviceMasterDataExtractor(DeviceMasterDataExtractor extractor) {
        DEVICE_MASTER_DATA_EXTRACTOR.set(extractor);
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

    public static Formatter formatter() {
        return FORMATTER.get();
    }

    public static void formatter(Formatter dateFormatter) {
        FORMATTER.set(dateFormatter);
    }

    public static X509Service x509Service() {
        return X509.get();
    }

    public static void x509Service(X509Service x509Service) {
        X509.set(x509Service);
    }

    public static KeyStoreService keyStoreService() {
        return KEY_STORE_SERVICE.get();
    }

    public static void keyStoreService(KeyStoreService service) {
        KEY_STORE_SERVICE.set(service);
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
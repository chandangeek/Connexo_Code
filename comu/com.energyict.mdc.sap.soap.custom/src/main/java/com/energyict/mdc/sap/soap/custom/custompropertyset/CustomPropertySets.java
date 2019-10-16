/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.custom.MessageSeeds;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component(name = "com.energyict.mdc.sap.soap.custom.custompropertyset.CustomPropertySets",
        service = {CustomPropertySets.class, TranslationKeyProvider.class},
        property = "name=" + CustomPropertySets.COMPONENT_NAME, immediate = true)
public class CustomPropertySets implements TranslationKeyProvider {
    private static final Logger LOGGER = Logger.getLogger(CustomPropertySets.class.getName());
    static final String COMPONENT_NAME = "CU1"; // only for translations

    public static final String COLON_SEPARATOR = ":";
    public static final String SEMICOLON_SEPARATOR = ";";
    public static final String COMMA_SEPARATOR = ",";
    public static final String SAP_CALCULATEDEVENTS_POWERFACTOR = "sap.calculatedevents.powerfactor";
    public static final String SAP_CALCULATEDEVENTS_MAXDEMAND = "sap.calculatedevents.maxdemand";
    public static final String SAP_CALCULATEDEVENTS_CTRATIO = "sap.calculatedevents.ctratio";
    private static Map<String, Pair<String, String>> powerFactorEventReadingTypes = new HashMap<>();
    private static Map<String, String> maxDemandEventReadingTypes = new HashMap<>();
    private static Map<String, String> ctRatioEventReadingTypes = new HashMap<>();

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;
    private volatile OrmService ormService;
    private volatile Thesaurus thesaurus;

    private CustomPropertySet<Device, PowerFactorDomainExtension> powerFactorInfo;
    private CustomPropertySet<Device, MaxDemandDomainExtension> maxDemandInfo;
    private CustomPropertySet<Device, CTRatioDomainExtension> ctRatioInfo;

    public CustomPropertySets() {
        // for OSGi purposes
    }

    @Inject
    public CustomPropertySets(BundleContext bundleContext, DeviceService deviceService, CustomPropertySetService customPropertySetService,
                              PropertySpecService propertySpecService, OrmService ormService,
                              NlsService nlsService) {
        setDeviceService(deviceService);
        setCustomPropertySetService(customPropertySetService);
        setPropertySpecService(propertySpecService);
        setOrmService(ormService);
        setNlsService(nlsService);
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        customPropertySetService.addCustomPropertySet(powerFactorInfo = new PowerFactorCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetService.addCustomPropertySet(maxDemandInfo = new MaxDemandCustomPropertySet(propertySpecService, thesaurus));
        customPropertySetService.addCustomPropertySet(ctRatioInfo = new CTRatioCustomPropertySet(propertySpecService, thesaurus));

        Optional<String> property = Optional.ofNullable(getPropertyValue(bundleContext, SAP_CALCULATEDEVENTS_POWERFACTOR));
        if (property.isPresent()) {
            powerFactorEventReadingTypes = parsePairReadingTypes(SAP_CALCULATEDEVENTS_POWERFACTOR, property.get());
        }

        property = Optional.ofNullable(getPropertyValue(bundleContext, SAP_CALCULATEDEVENTS_MAXDEMAND));
        if (property.isPresent()) {
            maxDemandEventReadingTypes = parseReadingTypes(SAP_CALCULATEDEVENTS_MAXDEMAND, property.get());
        }

        property = Optional.ofNullable(getPropertyValue(bundleContext, SAP_CALCULATEDEVENTS_CTRATIO));
        if (property.isPresent()) {
            ctRatioEventReadingTypes = parseReadingTypes(SAP_CALCULATEDEVENTS_CTRATIO, property.get());
        }
    }

    @Deactivate
    public void deactivate() {
        customPropertySetService.removeCustomPropertySet(powerFactorInfo);
        customPropertySetService.removeCustomPropertySet(maxDemandInfo);
        customPropertySetService.removeCustomPropertySet(ctRatioInfo);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(getComponentName(), getLayer());
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }

    private String getPropertyValue(BundleContext context, String propertyName) {
        String value = context.getProperty(propertyName);
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            LOGGER.log(Level.WARNING, MessageSeeds.PROPERTY_IS_NOT_SET.getDefaultFormat(), propertyName);
        }
        return value;
    }

    private String getValidValue(String name, String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.PROPERTY_VALUE_CANNOT_BE_EMPTY).format(name));
        }
        return value.trim();
    }

    private Map<String, Pair<String, String>> parsePairReadingTypes(String name, String value) {
        Map<String, Pair<String, String>> map = new HashMap<>();
        List<String> list = Arrays.asList(value.split(SEMICOLON_SEPARATOR));
        if (list.size() > 0) {
            for (String item : list) {
                List<String> params = Arrays.asList(item.split(COLON_SEPARATOR));
                if (params.size() == 2) {
                    List<String> readings = Arrays.asList(params.get(1).split(COMMA_SEPARATOR));
                    if (readings.size() == 2) {
                        map.put(getValidValue(name, params.get(0)), Pair.of(getValidValue(name, readings.get(0)), getValidValue(name, readings.get(1))));
                    } else {
                        throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID).format(name));
                    }
                } else {
                    throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID).format(name));
                }
            }
        } else {
            throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID).format(name));
        }
        return map;
    }

    private Map<String, String> parseReadingTypes(String name, String value) {
        Map<String, String> map = new HashMap<>();
        List<String> list = Arrays.asList(value.split(SEMICOLON_SEPARATOR));
        if (list.size() > 0) {
            for (String item : list) {
                List<String> params = Arrays.asList(item.split(COLON_SEPARATOR));
                if (params.size() == 2) {
                    map.put(getValidValue(name, params.get(0)), getValidValue(name, params.get(1)));
                } else {
                    throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID).format(name));
                }
            }
        } else {
            throw new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID).format(name));
        }
        return map;
    }

    public static Map<String, Pair<String, String>> getPowerFactorEventReadingTypes() {
        return powerFactorEventReadingTypes;
    }

    public static Map<String, String> getMaxDemandEventReadingTypes() {
        return maxDemandEventReadingTypes;
    }

    public static Map<String, String> getCTRatioEventReadingTypes() {
        return ctRatioEventReadingTypes;
    }
}

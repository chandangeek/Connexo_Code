/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configproperties;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.configproperties.AbstractConfigPropertiesProvider;
import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;
import com.elster.jupiter.metering.configproperties.PropertiesInfo;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.DeviceDataServices;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component(name = "com.energyict.mdc.device.data.impl.configproperties", service = ConfigPropertiesProvider.class, immediate = true)
public class CommunicationSettings extends AbstractConfigPropertiesProvider implements ConfigPropertiesProvider {
    static final String SCOPE_NAME = "COMMUNICATION";

    private static final Function<YesNoTranslationKeys, String> KEY_EXTRACTOR = Enum::name;
    private static final Function<String, YesNoTranslationKeys> OBJECT_FETCHER = YesNoTranslationKeys::valueOf;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private IdWithNameFactory<YesNoTranslationKeys> booleanValuesFactory;

    public CommunicationSettings() {
        this.booleanValuesFactory = new IdWithNameFactory<>(KEY_EXTRACTOR, this::translateConnectionState, OBJECT_FETCHER);
    }

    @Inject
    public CommunicationSettings(NlsService nlsService, OrmService ormService, PropertySpecService propertySpecService) {
        this();
        setOrmService(ormService);
        setPropertySpecService(propertySpecService);
        setThesaurus(nlsService);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.getDataModel(MeteringService.COMPONENTNAME).get();
    }

    @Reference
    void setThesaurus(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getScope() {
        return SCOPE_NAME;
    }

    @Override
    public List<PropertiesInfo> getPropertyInfos() {
       return Collections.singletonList(
                new PropertiesInfo("communicationSettings", thesaurus.getFormat(CommunicationSettingTranslationKeys.COMMUNICATION_SELECTOR).format(), Arrays.asList(
                        getTrueMinimizedProperty(), getRandomizationProperty())));
    }

    protected final Thesaurus getThesaurus() {
        return thesaurus;
    }

    private PropertySpec getTrueMinimizedProperty() {
        return propertySpecService
                .specForValuesOf(this.booleanValuesFactory)
                .named(CommunicationSettingTranslationKeys.TRUE_MINIMIZED)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(getPossibleValues())
                .setDefaultValue(this.booleanValuesFactory.wrap(YesNoTranslationKeys.NO))
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    private PropertySpec getRandomizationProperty() {
        return propertySpecService
                .specForValuesOf(this.booleanValuesFactory)
                .named(CommunicationSettingTranslationKeys.RANDOMIZATION)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(getPossibleValues())
                .setDefaultValue(this.booleanValuesFactory.wrap(YesNoTranslationKeys.NO))
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    private List<HasIdAndName> getPossibleValues() {
        return Arrays.asList(
                this.booleanValuesFactory.wrap(YesNoTranslationKeys.YES),
                this.booleanValuesFactory.wrap(YesNoTranslationKeys.NO));
    }

    private String translateConnectionState(YesNoTranslationKeys translationKeys) {
        if (translationKeys == null) {
            return null;
        }
        return getThesaurus().getString(translationKeys.getKey(), translationKeys.getDefaultFormat());
    }


}

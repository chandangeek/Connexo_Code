package com.elster.jupiter.systemproperties.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertySpec;
import com.elster.jupiter.systemproperties.impl.SystemPropertyTranslationKeys;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;


import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvictionTimeSystemPropertySpec implements SystemPropertySpec {

    private OrmService ormService;
    private PropertyValueInfoService propertyValueInfoService;
    private volatile Thesaurus thesaurus;
    PropertySpec propertySpec;
    PropertySpecService propertySpecService;

    @Inject
    public EvictionTimeSystemPropertySpec(OrmService ormService,
                                          PropertyValueInfoService propertyValueInfoService,
                                          PropertySpecService propertySpecService,
                                          Thesaurus thesaurus) {
        this.ormService = ormService;
        this.thesaurus = thesaurus;
        this.propertyValueInfoService = propertyValueInfoService;
        this.propertySpecService = propertySpecService;
        propertySpec = propertySpecService.timeDurationSpec()
                .named(SystemPropertyTranslationKeys.EVICTION_TIME)
                .describedAs(SystemPropertyTranslationKeys.EVICTION_TIME_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(TimeDuration.seconds(OrmService.EVICTION_TIME_IN_SECONDS_DEFAULT_VALUE))
                .markRequired()
                .finish();
    }

    @Override
    public String getKey() {
        return SystemPropertyTranslationKeys.EVICTION_TIME.getKey();
    }

    @Override
    public void actionOnChange(SystemProperty property) {
        long evictionTime = Long.valueOf(property.getValue());
        List<DataModel> datamodels = ormService.getDataModels();
        for (DataModel dataModel : datamodels) {
            for (Table table : dataModel.getTables()) {
                if (table.isCached() || table.isWholeTableCached()) {
                    table.changeEvictionTime(evictionTime);
                }
            }
        }
    }

    @Override
    public PropertyInfo preparePropertyInfo(SystemProperty property) {
        int count = Integer.valueOf(property.getValue());
        TimeDuration timeDuration = new TimeDuration(count, TimeDuration.TimeUnit.SECONDS);
        PropertyInfo info = propertyValueInfoService.getPropertyInfo(propertySpec, key -> timeDuration);
        return info;
    }

    @Override
    public String convertValueToString(PropertyInfo propertyInfo) {

        HashMap timeDurationInfo = (HashMap) propertyInfo.getPropertyValueInfo().getValue();

        Integer count = (Integer) timeDurationInfo.get("count");
        String timeUnitDescription = (String) timeDurationInfo.get("timeUnit");

        TimeDuration timeDuration = new TimeDuration(count, TimeDuration.TimeUnit.forDescription(timeUnitDescription));
        Integer time = timeDuration.getSeconds();

        return time.toString();
    }
}


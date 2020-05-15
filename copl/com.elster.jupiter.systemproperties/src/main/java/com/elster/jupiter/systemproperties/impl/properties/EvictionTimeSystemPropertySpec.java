package com.elster.jupiter.systemproperties.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertySpec;
import com.elster.jupiter.systemproperties.impl.SystemPropertyTranslationKeys;
import com.elster.jupiter.time.TimeDuration;


import javax.inject.Inject;
import java.util.List;

public class EvictionTimeSystemPropertySpec implements SystemPropertySpec {

    private OrmService ormService;
    private volatile Thesaurus thesaurus;
    PropertySpec propertySpec;
    PropertySpecService propertySpecService;

    @Inject
    public EvictionTimeSystemPropertySpec(OrmService ormService,
                                          PropertySpecService propertySpecService,
                                          Thesaurus thesaurus) {
        String[] defaultValue = OrmService.EVICTION_TIME_IN_SECONDS_DEFAULT_VALUE.split(":");
        int defaultValueSeconds = Integer.valueOf(defaultValue[0]);
        this.ormService = ormService;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        propertySpec = propertySpecService.timeDurationSpec()
                .named(SystemPropertyTranslationKeys.EVICTION_TIME)
                .describedAs(SystemPropertyTranslationKeys.EVICTION_TIME_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(TimeDuration.seconds(defaultValueSeconds))
                .markRequired()
                .finish();
    }

    @Override
    public String getKey() {
        return SystemPropertyTranslationKeys.EVICTION_TIME.getKey();
    }

    @Override
    public void actionOnChange(SystemProperty property) {
        TimeDuration timeDuration = (TimeDuration) propertySpec.getValueFactory().fromStringValue(property.getValue());
        long evictionTime = timeDuration.getMilliSeconds();
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
    public PropertySpec getPropertySpec(){
        return propertySpec;
    }
}


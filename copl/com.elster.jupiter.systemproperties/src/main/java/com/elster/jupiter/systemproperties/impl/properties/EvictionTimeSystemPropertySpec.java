package com.elster.jupiter.systemproperties.impl.properties;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.impl.SystemPropertySpec;
import com.elster.jupiter.systemproperties.impl.SystemPropertyTranslationKeys;
import com.elster.jupiter.time.TimeDuration;


import javax.inject.Inject;
import java.util.List;

public class EvictionTimeSystemPropertySpec implements SystemPropertySpec {
    private OrmService ormService;
    private PropertySpec propertySpec;

    @Inject
    public EvictionTimeSystemPropertySpec(OrmService ormService,
                                          PropertySpecService propertySpecService,
                                          Thesaurus thesaurus) {
        this.ormService = ormService;
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
        TimeDuration timeDuration = (TimeDuration)property.getValue();
        long evictionTime = timeDuration.getMilliSeconds();
        List<DataModel> dataModels = ormService.getDataModels();
        for (DataModel dataModel : dataModels) {
            for (Table table : dataModel.getTables()) {
                if (table.isCached()) {
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


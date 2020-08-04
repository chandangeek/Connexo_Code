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

import javax.inject.Inject;
import java.util.List;

public class CacheIsEnabledSystemPropertySpec implements SystemPropertySpec {
    private OrmService ormService;
    private PropertySpec propertySpec;

    @Inject
    public CacheIsEnabledSystemPropertySpec(OrmService ormService,
                                            PropertySpecService propertySpecService,
                                            Thesaurus thesaurus) {
        this.ormService = ormService;
        propertySpec = propertySpecService.booleanSpec()
                .named(SystemPropertyTranslationKeys.ENABLE_CACHE)
                .describedAs(SystemPropertyTranslationKeys.ENABLE_CACHE_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(OrmService.ENABLE_CACHE_DEFAULT_VALUE)
                .finish();
    }

    @Override
    public String getKey() {
        return SystemPropertyTranslationKeys.ENABLE_CACHE.getKey();
    }

    @Override
    public void actionOnChange(SystemProperty property) {
        //recreate all caches
        boolean cacheEnabled = (Boolean) property.getValue();
        List<DataModel> dataModels = ormService.getDataModels();
        for (DataModel dataModel : dataModels) {
            for (Table table : dataModel.getTables()) {
                if (table.getCacheType() != Table.CacheType.NO_CACHE) {
                    if (cacheEnabled && !table.isCached()) {
                        table.enableCache();
                    }
                    if (!cacheEnabled && table.isCached()) {
                        table.disableCache();
                    }
                }
            }
        }
    }

    @Override
    public PropertySpec getPropertySpec(){
        return propertySpec;
    }
}

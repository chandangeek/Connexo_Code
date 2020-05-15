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

import javax.inject.Inject;
import java.util.List;

public class CacheIsEnabledSystemPropertySpec implements SystemPropertySpec {

    private OrmService ormService;
    private PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private PropertySpec propertySpec;

    @Inject
    public CacheIsEnabledSystemPropertySpec(OrmService ormService,
                                            PropertySpecService propertySpecService,
                                            Thesaurus thesaurus) {
        this.ormService = ormService;
        this.propertySpecService = propertySpecService;
        propertySpec = propertySpecService.booleanSpec()
                .named(SystemPropertyTranslationKeys.ENABLE_CACHE)
                .describedAs(SystemPropertyTranslationKeys.ENABLE_CACHE_DESCRIPTION)
                .fromThesaurus(thesaurus)
                .setDefaultValue(true)
                .finish();
    }

    @Override
    public String getKey() {
        return SystemPropertyTranslationKeys.ENABLE_CACHE.getKey();
    }

    @Override
    public void actionOnChange(SystemProperty property) {
        //recreate all caches
        boolean cacheEnabled = property.getValue().equals("1") ? true : false;
        List<DataModel> datamodels = ormService.getDataModels();
        for (DataModel dataModel : datamodels) {
            for (Table table : dataModel.getTables()) {
                if (table.getCacheType() != Table.CacheType.NO_CACHE) {
                    if (cacheEnabled) {
                        if ((table.isWholeTableCached() == false && table.getCacheType() == Table.CacheType.WHOLE_TABLE_CACHE)
                                || (table.isCached() == false && table.getCacheType() == Table.CacheType.TUPLE_CACHE)) {
                            table.enableCache();
                        }
                    }

                    if (!cacheEnabled) {
                        if (table.isWholeTableCached() || table.isCached()) {
                            table.disableCache();
                        }
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

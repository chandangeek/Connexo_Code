package com.elster.jupiter.systemproperties.properties;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.systemproperties.SystemProperty;
import com.elster.jupiter.systemproperties.SystemPropertySpec;

import java.util.List;

public class CacheIsEnabledSystemPropertySpec implements SystemPropertySpec {

    private OrmService ormService;
    public final static String PROPERTY_KEY = "enablecache";
    private final static String PROPERTY_DESCRIPTION = "Enable caching";
    private final String PROPERTY_NAME = "Enable caching";
    public String getName(){
        return PROPERTY_NAME;
    };
    public String getKey(){
        return PROPERTY_NAME;
    };
    public String getDescription(){
        return PROPERTY_DESCRIPTION;
    };
    public PropertyType getPropertyType(){
        return SimplePropertyType.BOOLEAN;
    };


    public CacheIsEnabledSystemPropertySpec(OrmService ormService){
        this.ormService = ormService;
    }

    public void actionOnChange(SystemProperty property){
        //recreate all caches
        boolean cacheEnabled = Boolean.valueOf(property.getValue());
        List<DataModel> datamodels = ormService.getDataModels();
        for(DataModel dataModel : datamodels){
            for(Table table : dataModel.getTables()){
                if (table.getCacheType() != Table.CacheType.NO_CACH){
                    if (cacheEnabled){
                        if ((table.isWholeTableCached() == false && table.getCacheType() == Table.CacheType.WHOLE_TABLE_CACHE)
                                || (table.isCached() == false && table.getCacheType() == Table.CacheType.TUPLE_CACHE)){
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
    };

    @Override
    public PropertyInfo preparePropertyInfo(SystemProperty property){
        boolean value = Boolean.valueOf(property.getValue());
        PropertyValueInfo propertyValueInfo = new PropertyValueInfo(value,
                true,
                true,
                null);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = this.getPropertyType();
        PropertyInfo info  = new PropertyInfo(PROPERTY_NAME,
                PROPERTY_KEY,
                PROPERTY_DESCRIPTION,
                propertyValueInfo,
                propertyTypeInfo,
                false);

        return info;
    };

    @Override
    public String convertValueToString(PropertyInfo propertyInfo){
        String value =  (Boolean)propertyInfo.getPropertyValueInfo().getValue() ? "true" : "false";
        return  value;
    }
}

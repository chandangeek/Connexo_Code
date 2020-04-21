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
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;


import java.util.HashMap;
import java.util.List;

public class EvictionTimeSystemPropertySpec implements SystemPropertySpec {

    OrmService ormService;
    public static String PROPERTY_KEY = "evictiontime";
    private static String PROPERTY_DESCRIPTION = "Eviction time for table cache";
    private String PROPERTY_NAME = "Eviction time";

    public EvictionTimeSystemPropertySpec(OrmService ormService){
        this.ormService = ormService;
    }


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
        return SimplePropertyType.DURATION;
    };


    private final static  TimeDurationInfo defaultValue = new TimeDurationInfo(new TimeDuration(300, TimeDuration.TimeUnit.SECONDS));


    public void actionOnChange(SystemProperty property) {
        long evictionTime = Long.valueOf(property.getValue());
        List<DataModel> datamodels = ormService.getDataModels();
        for (DataModel dataModel : datamodels) {
            for (Table table : dataModel.getTables()) {
                if (table.isCached() || table.isWholeTableCached()) {
                    if (table.getCacheType() != Table.CacheType.NO_CACH) {

                        if (table.isCached()) {
                            table.changeEvictionTime(evictionTime);
                        }

                    }
                }
            }
        }
    };

    @Override
    public PropertyInfo preparePropertyInfo(SystemProperty property){

        int count = Integer.valueOf(property.getValue());
        TimeDuration timeDuration = new TimeDuration(count, TimeDuration.TimeUnit.SECONDS);
        TimeDurationInfo tdInfo = new TimeDurationInfo(timeDuration);


        PropertyValueInfo propertyValueInfo = new PropertyValueInfo(tdInfo/*property.getValue()*/,
                null,
                defaultValue,
                null);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = this.getPropertyType();
        PropertyInfo info  = new PropertyInfo(PROPERTY_NAME,
                PROPERTY_KEY,
                PROPERTY_DESCRIPTION,
                propertyValueInfo,
                propertyTypeInfo,
                true);

        return info;
    };

    @Override
    public String convertValueToString(PropertyInfo propertyInfo){

        HashMap timeDurationInfo = (HashMap)propertyInfo.getPropertyValueInfo().getValue();

        Integer count = (Integer)timeDurationInfo.get("count");
        String timeUnitDescription = (String)timeDurationInfo.get("timeUnit");

        TimeDuration timeDuration = new TimeDuration(count, TimeDuration.TimeUnit.forDescription(timeUnitDescription));
        Integer time = timeDuration.getSeconds();

        return time.toString();
    }

}


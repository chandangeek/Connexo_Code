package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.message;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;

/**
 * Created by cisac on 19/01/2017.
 */
public class DayProfile extends Structure {

    public DayProfile(String dayProfileDefinition){
        parseDayProfileDefinition(dayProfileDefinition);
    }

    private void parseDayProfileDefinition(String dayProfileStringDefinition) {

        Unsigned8 dayId = new Unsigned8(Integer.parseInt(dayProfileStringDefinition.substring(0, dayProfileStringDefinition.indexOf(",")).trim()));
        String dayScheduleStringDefinition = dayProfileStringDefinition.substring(dayProfileStringDefinition.indexOf(",")+1, dayProfileStringDefinition.length()).trim();
        String[] dayProfileActionStringDefinitions = dayScheduleStringDefinition.split(String.valueOf('>'));
        Array daySchedule = new Array();
        for(String dayProfileActionDefinition: dayProfileActionStringDefinitions){
            DayProfileAction dayProfileAction = new DayProfileAction(dayProfileActionDefinition);
            daySchedule.addDataType(dayProfileAction);
        }
        addDataType(dayId);
        addDataType(daySchedule);
    }

}

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.tasks.ManualMeterReadingsTask;
import com.energyict.mdc.tasks.OfflineManualMeterReadingsTask;
import com.energyict.mdc.upl.meterdata.RegisterGroup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 7/10/2016 - 11:47
 */
@XmlRootElement
public class OfflineManualMeterReadingsTaskImpl implements OfflineManualMeterReadingsTask {

    private List<Long> registerGroups;

    public OfflineManualMeterReadingsTaskImpl(ManualMeterReadingsTask manualMeterReadingsTask) {
        goOffline(manualMeterReadingsTask);
    }

    /**
     * Triggers the capability to go offline and will copy all information
     * from the database into memory so that normal business operations can continue.<br>
     * Note that this may cause recursive calls to other objects that can go offline.
     * @param manualMeterReadingsTask
     */
    protected void goOffline(ManualMeterReadingsTask manualMeterReadingsTask) {
        setRegisterGroups(getIdsFromList(manualMeterReadingsTask.getRegisterGroups()));
    }

    private List<Long> getIdsFromList(final List<RegisterGroup> registerGroups) {
        List<Long> idList = new ArrayList<>();
        for (RegisterGroup registerGroup : registerGroups) {
            idList.add(((HasId)registerGroup).getId());
        }
        return idList;
    }

    @Override
    @XmlAttribute
    public List<Long> getRegisterGroups() {
        return registerGroups;
    }

    public void setRegisterGroups(List<Long> registerGroups) {
        this.registerGroups = registerGroups;
    }
}
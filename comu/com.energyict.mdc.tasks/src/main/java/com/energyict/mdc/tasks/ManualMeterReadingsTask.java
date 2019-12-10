package com.energyict.mdc.tasks;

import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.upl.meterdata.RegisterGroup;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Collection;
import java.util.List;

public interface ManualMeterReadingsTask extends ProtocolTask {


    /**
     * Return a list of {@link RegisterGroup}s which need to be fetched during this task.
     * If no groups are defined, then an empty list will be returned.
     *
     * @return the list of RtuRegisterGroups
     */
    @XmlAttribute
    public List<RegisterGroup> getRegisterGroups();
    void setRegisterGroups(Collection<RegisterGroup> registerGroups);

    interface ManualMeterReadingsTaskBuilder {
        public ManualMeterReadingsTask.ManualMeterReadingsTaskBuilder registerGroups(Collection<RegisterGroup> registerGroups);
        public ManualMeterReadingsTask add();
    }

}
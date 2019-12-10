package com.energyict.mdc.tasks;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;

public interface OfflineManualMeterReadingsTask extends OfflineProtocolTask<ManualMeterReadingsTask> {

    @XmlAttribute
    public List<Long> getRegisterGroups();
}

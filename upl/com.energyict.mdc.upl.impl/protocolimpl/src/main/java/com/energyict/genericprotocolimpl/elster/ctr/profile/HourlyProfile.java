package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.StartDate;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class HourlyProfile {

    private final GprsRequestFactory requestFactory;


    public HourlyProfile(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public void read() throws CTRException {
        getProfileInfo();

        CTRObjectID objectID = new CTRObjectID("1.2.2");
        PeriodTrace period = new PeriodTrace(2);
        StartDate startDate = new StartDate().parse(new byte[] {10, 10, 19}, 0);
        Trace_CQueryResponseStructure response = getRequestFactory().queryTrace_C(objectID, period, startDate);
        System.out.println(response);
    }

    public void getProfileInfo() throws CTRException {
        List<AbstractCTRObject> ctrObjectList = getRequestFactory().queryRegisters(AttributeType.getValueOnly(), "15.0.2");
        for (AbstractCTRObject ctrObject : ctrObjectList) {
            System.out.println(ctrObject);
        }

    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }
}

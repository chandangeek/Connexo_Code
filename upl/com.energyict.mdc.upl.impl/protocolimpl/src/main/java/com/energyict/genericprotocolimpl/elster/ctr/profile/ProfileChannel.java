package com.energyict.genericprotocolimpl.elster.ctr.profile;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.Trace_CQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.PeriodTrace;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.ReferenceDate;
import com.energyict.mdw.core.Channel;
import com.energyict.protocol.ProfileData;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 18-okt-2010
 * Time: 16:14:08
 */
public class ProfileChannel {

    private final GprsRequestFactory requestFactory;


    public ProfileChannel(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        requestFactory.getProperties().getChannelConfig();
    }

    public ProfileData getProfileData(Channel meterChannel) throws CTRException {
        int loadProfileIndex = meterChannel.getLoadProfileIndex(); // 1-based

        String channelId = getProperties().getChannelConfig().getChannelObjectId(loadProfileIndex);
        System.out.println(channelId);


        getProfileInfo("15.0.2", "15.0.3");

        String[] ids = {/*"1.0.2", "1.2.2", "4.0.2",*/ "7.0.2"/*, "1.1.3", "1.3.3", "1.F.2", "2.0.3", "2.1.3", "2.3.3", "1.A.3", "12.6.3"*/};
        for (String id : ids) {
            CTRObjectID objectID = new CTRObjectID(id);
            PeriodTrace period = new PeriodTrace(1);
            ReferenceDate referenceDate = new ReferenceDate().parse(new byte[]{10, 10, 18}, 0);
            Trace_CQueryResponseStructure response = getRequestFactory().queryTrace_C(objectID, period, referenceDate);
            for (AbstractCTRObject object : response.getTraceData()) {
                if (!object.getQlf().isInvalid()) {
                    System.out.println(object.getValue()[0].getValue());
                }
            }
        }
        return new ProfileData();
    }

    public void getProfileInfo(String... profiles) throws CTRException {
        List<AbstractCTRObject> ctrObjectList = getRequestFactory().queryRegisters(AttributeType.getValueOnly(), profiles);
        for (AbstractCTRObject ctrObject : ctrObjectList) {
            System.out.println(ctrObject);
        }
    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    private MTU155Properties getProperties() {
        return getRequestFactory().getProperties();
    }

}

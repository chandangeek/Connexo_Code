package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.energyict.mdw.core.Device;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights
 * Date: 15/06/11
 * Time: 11:53
 */
public class EventTaskExecutor extends AbstractExecutor<Device> {

    public EventTaskExecutor(AbstractExecutor executor) {
        super(executor);
    }

    @Override
    public void execute(Device rtu) throws IOException {
        if (rtu != null) {
            Date lastLogbook = rtu.getLastLogbook();
            if (lastLogbook == null) {
                lastLogbook = new Date(0);
            }
            List<MeterEvent> meterEvents = getDlmsProtocol().getMeterEvents(lastLogbook);
            if (meterEvents != null) {
                ProfileData pd = new ProfileData();
                pd.setChannelInfos(new ArrayList<ChannelInfo>());
                pd.setIntervalDatas(new ArrayList<IntervalData>());
                pd.setMeterEvents(meterEvents);
                pd.sort();
                getStoreObject().add(rtu, pd);
            } else {
                severe("List of meter events was 'null'!");
            }
        } else {
            throw new IOException("Unable to read device events. Device was 'null'!");
        }
    }

}

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 28-apr-2011
 * Time: 11:27:20
 */
public class ReadTOUBuckets extends AbstractRadioCommand {

    public ReadTOUBuckets(RTM rtm) {
        super(rtm);
    }

    private List<PortTotalizers> listOfAllTotalizers;
    private int numberOfPorts;

    /**
     * List of totalizers, grouped per port.
     * Element 1 is an array of 6 totalizers, for the TOU buckets on port 1.
     */
    public List<PortTotalizers> getListOfAllTotalizers() {
        return listOfAllTotalizers;
    }

    public int getNumberOfPorts() {
        return numberOfPorts;
    }

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        parse(data, null);
    }

    public void parse(byte[] data, byte[] radioAddress, RTMFactory rtmFactory) throws IOException {
        if (data.length <= 23) {
            return;         //The evoHop module doesn't contain TOU bucket data
        }

        getGenericHeader().setRadioAddress(radioAddress);
        getGenericHeader().parse(data, rtmFactory);
        numberOfPorts = getGenericHeader().getOperationMode().readNumberOfPorts();
        int offset = 23;    //Skip the generic header

        TimeZone timeZone = getRTM().getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        Date initializationDate = TimeDateRTCParser.parse(data, offset, 7, timeZone).getTime();
        offset += 7;
        offset += 7;        //Skip the bucket parameters

        listOfAllTotalizers = new ArrayList<PortTotalizers>();

        for (int port = 0; port < numberOfPorts; port++) {
            int[] totalizers = new int[6];
            int currentReading = 0;
            currentReading = ProtocolTools.getIntFromBytes(data, offset, 4);
            offset += 4;
            for (int bucket = 1; bucket < 7; bucket++) {
                totalizers[bucket - 1] = ProtocolTools.getIntFromBytes(data, offset, 4);
                offset += 4;
            }
            listOfAllTotalizers.add(new PortTotalizers(currentReading, port, totalizers));
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ReadTouBuckets;
    }
}
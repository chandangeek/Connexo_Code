package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.loadprofiles;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Iulian on 5/18/2017.
 */
@Deprecated
public class LTEMonitoringProfileIntervals extends DLMSProfileIntervals {

    private Logger logger = Logger.getAnonymousLogger();

    public LTEMonitoringProfileIntervals(byte[] encodedData, int defaultClockMask, Integer integer, Integer integer1, ProfileIntervalStatusBits statusBits, Logger logger) throws IOException {
        super(encodedData, statusBits);
        this.logger = logger;
    }

    public LTEMonitoringProfileIntervals(byte[] encodedData, int clockMask, int statusMask, int channelMask, ProfileIntervalStatusBits statusBits) throws IOException {
        super(encodedData, clockMask, statusMask, channelMask, statusBits);
    }

    public Logger getLogger(){
        return logger;
    }

    @Override
    public List<IntervalData> parseIntervals(int profileInterval, TimeZone timeZone) throws IOException {
        this.profileInterval = profileInterval;
        List<IntervalData> intervalList = new ArrayList<IntervalData>();
        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (getAllDataTypes().size() != 0) {

            for (int i = 0; i < nrOfDataTypes(); i++) {
                Structure element = (Structure) getDataType(i);


                // 0 = calendar
                cal = constructIntervalCalendar(cal, element.getDataType(0), timeZone);
                currentInterval = new IntervalData(cal.getTime(), profileStatus);

                Structure   gsmDiagOperator   = element.getDataType(1).getStructure();
                Structure   gsmDiagCellInfo   = element.getDataType(2).getStructure();
                Array       adjantCells       = element.getDataType(3).getArray();
                Structure   rejection         = element.getDataType(4).getStructure();

//                currentInterval.addValues(decodeGSMDiagnosticOperator(gsmDiagOperator)); todo check if decode methods need to to implemented
//                currentInterval.addValues(decodeGSMDiagnosticCellInfo(gsmDiagCellInfo));
//                currentInterval.addValues(decodeGSMDiagnosticAdjacentCells(adjantCells, cal));
//                currentInterval.addValues(decodeLTEConectionRejection(rejection, timeZone));

                intervalList.add(currentInterval);
            }
        }

        return intervalList;
    }

    /**
     * GSM diagnostic, operator 151, 0.1.25.11.0.255, 2, 0. This attribute consists itself of a structure of five (so this will mean 5 channels in EIServer):
             T3402: long-unsigned (timer in seconds, used on PLMN selection procedure and sent by the network to the modem)
             T3412: long-unsigned (timer in seconds used to manage the periodic tracking area updating procedure and sent by the network to the modem)
             RSRQ: unsigned (represents the signal quality)
             RSRP: unsigned (represents the signal level )
             qRxlevMin: integer (specifies the minimum required Rx level in the cell in dBm)

     * @param gsmDiagOperator
     * @return
     */
    private Collection decodeGSMDiagnosticOperator(Structure gsmDiagOperator) {
        Collection<Number> values = new ArrayList<Number>();

        for (int ch = 0; ch < gsmDiagOperator.nrOfDataTypes(); ch++) {
            values.add(getNumericalValue(gsmDiagOperator.getDataType(ch)));
        }

        return values;
    }

    /**
     * GSM diagnostic, cell_info 47, 0.1.25.6.0.255, 6, 1. This attribute consists itself of a structure of seven (so this will mean 7 channels in EIServer):
             cell_ID: long-unsigned (Four-byte cell ID in hexadecimal format)
             location_ID: long-unsigned (Two-byte location area code (LAC) in the case of GSM networks or Tracking Area Code (TAC) in the case of UMTS, CDMA or LTE networks in hexadecimal format )
             signal_quality: unsigned (Represents the signal quality)
             ber: unsigned (Bit Error Rate (BER) measurement in percent)
             mcc: long-unsigned (Mobile Country Code of the serving network)
             mnc: long-unsigned (Mobile Network Code of the serving network)
             channel_number double-long-unsigned (Represents the absolute radio-frequency channel number (ARFCN or eaRFCN for LTE network))

     * @param gsmDiagCellInfo
     * @return
     */
    private Collection decodeGSMDiagnosticCellInfo(Structure gsmDiagCellInfo) {
        Collection<Number> values = new ArrayList<Number>();

        for (int ch = 0; ch < gsmDiagCellInfo.nrOfDataTypes(); ch++) {
            values.add(getNumericalValue(gsmDiagCellInfo.getDataType(ch)));
        }

        return values;
    }

    /**
     * GSM diagnostic, adjacent_cells 47, 0.1.25.6.0.255, 7, 1. This attribute is an array and within each cell of the array is a structure of two. Because the number of entries in the array will never be more than 3, this will be 6 channels in EIServer). The structure of two:
             cell_ID: double-long-unsigned (Four-byte cell ID in hexadecimal format)
             signal_quality: unsigned (Represents the signal quality)

     The array for adjacent_cells should exist of three entries. However to be able to handle other situations please process as follows:
            Less than 3 entries in the array.
                    Do not fill the corresponding two channels for the missing entries.
                    So if there is only one entry only one set of cell_ID and signal_quality channels will be filled.
            More than 3 entries in the array.
                    Only fill the first three sets of cell_ID and signal_quality.
                    So ignore possible extra entries (above three) of the array.
                    Make a note in the protocol-logging of the fact that there were more than 3 entries in the array and when possible
                    also place an info message on the communication session which still ended with status success.

     * @param adjacentCellsArray
     * @param cal
     * @return
     */
    private Collection decodeGSMDiagnosticAdjacentCells(Array adjacentCellsArray, Calendar cal) {
        Collection<Number> values = new ArrayList<Number>();

        Structure adjacentCell;
        int filledChannels = 0;

        for (int i=0; i< adjacentCellsArray.nrOfDataTypes(); i++) {
            adjacentCell = adjacentCellsArray.getDataType(i).getStructure();
            if (filledChannels < 3) {
                values.add(getNumericalValue(adjacentCell.getDataType(0)));
                values.add(getNumericalValue(adjacentCell.getDataType(1)));
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Adjacent Cell info not saved: ");
                sb.append(cal.getTime().toString());
                sb.append(", #").append(i);
                sb.append(": cellId=");
                sb.append(getNumericalValue(adjacentCell.getDataType(0)));
                sb.append(", signalQuality=");
                sb.append(getNumericalValue(adjacentCell.getDataType(1)));

                getLogger().info(sb.toString());
            }
            filledChannels++;
        }

        // add extra empty channels for the other up to 3
        for (int i = filledChannels; i<3; i++ ){
            values.add(0);
            values.add(0);
        }

        return values;
    }

    private Number getNumericalValue(AbstractDataType dataType) {
        if (dataType.isUnsigned8()){
            return dataType.getUnsigned8().getValue();
        }
        if (dataType.isUnsigned16()){
            return dataType.getUnsigned16().getValue();
        }
        if (dataType.isUnsigned32()){
            return dataType.getUnsigned32().getValue();
        }
        if (dataType.isInteger8()){
            return dataType.getInteger8().getValue();
        }
        if (dataType.isInteger16()){
            return dataType.getInteger16().getValue();
        }
        if (dataType.isInteger32()){
            return dataType.getInteger32().getValue();
        }
        if (dataType.isInteger64()){
            return dataType.getInteger64().getValue();
        }

        return dataType.longValue();
    }


    /**
     * LTE connection rejection 1, 0.1.94.31.7.255, 2, 0. This attribute consists itself of a structure of four (so this will mean 4 channels in EIServer):
             last_reject_cause unsigned (provides the last rejected cause on network)
             last_rejected_mcc long-unsigned (Mobile Country Code of the last rejected network)
             last_rejected_mnc long-unsigned (Mobile Network Code of the last rejected network)
             timestamp_last_rejection date_time (specifies the date and time of the rejection). This date_time can be stored in EIServer in epoch format.

     * @param rejection
     * @param timeZone
     * @return
     */
    private Collection decodeLTEConectionRejection(Structure rejection, TimeZone timeZone) {
        Collection<Number> values = new ArrayList<Number>();

        for(int ch = 0; ch < 3; ch++) {
            values.add(getNumericalValue(rejection.getDataType(ch)));
        }

        OctetString os = rejection.getDataType(3).getOctetString();
        values.add(os.getDateTime(timeZone).getValue().getTimeInMillis() / 1000);

        return values;
    }

}

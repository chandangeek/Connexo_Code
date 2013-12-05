package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ASDU;
import com.energyict.protocolimpl.din19244.poreg2.core.DinTimeParser;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.Response;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to read out a number of profile data entries.
 *
 * Copyrights EnergyICT
 * Date: 5-mei-2011
 * Time: 11:58:02
 */
public class ProfileData extends AbstractRequest {

    private int gid;
    private int registerAddress;
    private int fieldAddress;
    private int numberOfRegisters;
    private int numberOfFields;
    private int profileId;
    private Date from;
    private Date to;
    private List<ProfileDataEntry> profileDataEntries = new ArrayList<ProfileDataEntry>();
    private int length;

    private int receivedNumberOfRecords;
    private int receivedProfileInterval;

    public ProfileData(int length, int gid, Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields, int profileId, Date from, Date to) {
        super(poreg);
        this.length = length;
        this.gid = gid;
        this.registerAddress = registerAddress;
        this.fieldAddress = fieldAddress;
        this.numberOfRegisters = numberOfRegisters;
        this.numberOfFields = numberOfFields;
        this.profileId = profileId;
        this.from = from;
        this.to = to;
    }

    public String getCorruptCause() {
        return corruptCause;
    }

    public boolean isCorruptFrame() {
        return corruptFrame;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        Date recordingDate;
        int status;
        ExtendedValue extendedValue;

        for (int i = 0; i < getReceivedNumberOfRecords(); i++) {
            recordingDate = DinTimeParser.calcDate(poreg, ProtocolTools.getUnsignedIntFromBytesLE(data, offset, 4));
            offset += 4;

            status = ProtocolTools.getUnsignedIntFromBytesLE(data, offset, 2);
            offset += 2;

            extendedValue = RegisterDataParser.parseData(data, offset, getReceivedNumberOfRegisters(), getReceivedNumberOfFields()).get(0);
            offset += (!extendedValue.isValid()) ? 1 : extendedValue.getType().getLength() + 1;

            profileDataEntries.add(new ProfileDataEntry(length, gid, registerAddress, fieldAddress, recordingDate, status, extendedValue));
        }
    }

    @Override
    public void doRequest() throws IOException {
        corruptFrame = false;
        corruptCause = "";
        try {
            byte[] response = poreg.getConnection().doRequest(getRequestASDU(), getAdditionalBytes(), getExpectedResponseType(), getResponseASDU());
            while (true) {
                response = validateAdditionalBytes(response);
                parse(response);
                if (!poreg.getConnection().isDoContinue()) {
                    break;
                }
                response = poreg.getConnection().doContinue(getExpectedResponseType(), getResponseASDU());
            }
        } catch (ProtocolConnectionException e) { //E.g. crc error. Do not catch severe IOExceptions
             corruptFrame = true;
             corruptCause = e.getMessage();
        }
    }

    public List<ProfileDataEntry> getProfileDataEntries() {
        return profileDataEntries;
    }

    public int getReceivedNumberOfRecords() {
        return receivedNumberOfRecords;
    }

    public int getReceivedProfileInterval() {
        return receivedProfileInterval;
    }

    @Override
    public byte[] getAdditionalBytes() throws IOException {
        byte[] request = new byte[6];
        byte[] fromDateBytes = DinTimeParser.getBytes(poreg, from);
        byte[] toDateBytes = DinTimeParser.getBytes(poreg, to);

        request[0] = (byte) gid;
        request[1] = (byte) registerAddress;
        request[2] = (byte) fieldAddress;
        request[3] = (byte) numberOfRegisters;
        request[4] = (byte) numberOfFields;
        request[5] = (byte) profileId;
        request = ProtocolTools.concatByteArrays(request, fromDateBytes, toDateBytes);
        return request;
    }

    @Override
    protected byte[] validateAdditionalBytes(byte[] response) throws IOException {
        int receivedGid = response[0] & 0xFF;
        if (receivedGid != gid) {
            throw new IOException("Error receiving register data. Expected GID: " + gid + ", received " + receivedGid);
        }

        receivedRegisterAddress = response[1] & 0xFF;
        receivedFieldAddress = response[2] & 0xFF;
        receivedNumberOfRegisters = response[3] & 0xFF;
        receivedNumberOfFields = response[4] & 0xFF;
        receivedNumberOfRecords = response[5] & 0xFF;
        receivedProfileInterval = ProtocolTools.getUnsignedIntFromBytesLE(response, 6, 4);
        return ProtocolTools.getSubArray(response, getLengthOfReceivedAdditionalBytes());
    }

    @Override
    protected int getResponseASDU() {
        return ASDU.ProfileDataResponse.getId();
    }

    protected int getLengthOfReceivedAdditionalBytes() {
        return 10;
    }

    @Override
    protected int getExpectedResponseType() {
        return Response.USERDATA.getId();
    }

    @Override
    protected byte[] getRequestASDU() {
        return ASDU.ProfileData.getIdBytes();
    }
}
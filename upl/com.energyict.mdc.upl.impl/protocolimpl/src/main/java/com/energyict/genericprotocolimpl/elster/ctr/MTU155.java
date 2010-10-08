package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureCtrConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationRequestStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationResponseStructure;
import com.energyict.protocolimpl.debug.DebugUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.crypto.*;
import java.io.IOException;
import java.security.*;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 24-sep-2010
 * Time: 11:43:45
 */
public class MTU155 extends AbstractGenericProtocol {

    private MTU155Properties properties = new MTU155Properties();
    private IdentificationResponseStructure identification;
    private CtrConnection connection;

    public String getVersion() {
        return "$Date$";
    }

    public List<String> getRequiredKeys() {
        return properties.getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return properties.getOptionalKeys();
    }

    @Override
    public void initProperties() {
        properties.addProperties(getProperties());
    }

    @Override
    protected void doExecute() {
        this.connection = new SecureCtrConnection(getLink().getInputStream(), getLink().getOutputStream(), getProtocolProperties());
        try {
            getIdentification();
        } catch (CTRConnectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    private void testEncryption() {
        try {

            GPRSFrame readRequest = new GPRSFrame();
            readRequest.getFunctionCode().setFunction(Function.QUERY);
            readRequest.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
            readRequest.getStructureCode().setStructureCode(StructureCode.REGISTER);
            readRequest.setChannel(new Channel(0));
            readRequest.getProfi().setProfi(0x00);
            readRequest.getProfi().setLongFrame(false);

            Data data = new Data();
            byte[] pssw = ProtocolTools.getBytesFromHexString("$30$30$30$30$30$31");
            byte[] nrObjects = ProtocolTools.getBytesFromHexString("$01");
            AttributeType type = new AttributeType(0);
            type.setHasValueFields(true);
            byte[] attributeType = type.getBytes();
            byte[] id1 = new CTRObjectID("C.0.0").getBytes();

            byte[] rawData = ProtocolTools.concatByteArrays(pssw, nrObjects, attributeType, id1, new byte[128]);
            data.parse(rawData, 0);
            data.parse(ProtocolTools.getBytesFromHexString("$30$30$30$30$30$31$01$02$0C$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00"), 0);
            readRequest.setData(data);
            readRequest.calcCpa(getProtocolProperties().getKeyCBytes());

            System.out.println(readRequest);

            GPRSFrame response = null;
            response = getConnection().sendFrameGetResponse(readRequest);
            System.out.println(response);

        } catch (CTRException e) {
            e.printStackTrace();
        }
    }

    private MTU155Properties getProtocolProperties() {
        return properties;
    }

    public static void main(String[] args) throws IOException, LinkException, BusinessException, SQLException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {
        int baudRate = 9600;
        int dataBits = SerialCommunicationChannel.DATABITS_8;
        int parity = SerialCommunicationChannel.PARITY_NONE;
        int stopBits = SerialCommunicationChannel.STOPBITS_1;

        Dialer dialer = DebugUtils.getConnectedDirectDialer("COM1", baudRate, dataBits, parity, stopBits);

        MTU155 mtu155 = new MTU155();
        mtu155.execute(null, dialer, Logger.getAnonymousLogger());

    }

    public IdentificationResponseStructure getIdentification() throws CTRConnectionException {
        if (identification == null) {
            GPRSFrame request = new GPRSFrame();
            request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
            request.getFunctionCode().setFunction(Function.IDENTIFICATION_REQUEST);
            request.getProfi().setLongFrame(false);
            request.getStructureCode().setStructureCode(StructureCode.IDENTIFICATION);
            request.setData(new IdentificationRequestStructure());
            request.setCpa(new Cpa(0x00));
            GPRSFrame response = getConnection().sendFrameGetResponse(request);
            if (response.getData() instanceof IdentificationResponseStructure) {
                this.identification = (IdentificationResponseStructure) response.getData();
            }
        }
        return this.identification;
    }

    public CtrConnection getConnection() {
        return connection;
    }
}

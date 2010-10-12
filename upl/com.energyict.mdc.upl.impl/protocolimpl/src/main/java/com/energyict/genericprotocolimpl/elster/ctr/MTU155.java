package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.EncryptionStatus;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.mdw.core.Rtu;
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

    private final StoreObject storeObject = new StoreObject();
    private final MTU155Properties properties = new MTU155Properties();
    private GprsRequestFactory requestFactory;
    private Rtu rtu;

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
    protected void doExecute() throws IOException {
        this.requestFactory = new GprsRequestFactory(getLink(), getLogger(), getProtocolProperties());
        this.rtu = identifyRtu();
        log("Rtu with name '" + getRtu().getName() + "' connected successfully.");
        getProtocolProperties().addProperties(rtu.getProtocol().getProperties());
        getProtocolProperties().addProperties(rtu.getProperties());
        System.out.println(getProtocolProperties());
    }

    private Rtu identifyRtu() throws CTRException {
        String pdr = readPdr();
        log("MTU155 with pdr='" + pdr + "' connected.");

        List<Rtu> rtus = CommonUtils.mw().getRtuFactory().findByDialHomeId(pdr);
        switch (rtus.size()) {
            case 0:
                throw new CTRConnectionException("No rtu found in EiServer with callhomeId='" + pdr + "'");
            case 1:
                return rtus.get(0);
            default:
                throw new CTRConnectionException("Found " + rtus.size() + " rtu's in EiServer with callhomeId='" + pdr + "', but only one allowed. Skipping communication until fixed.");
        }

    }

    /**
     * @return the pdr value as String
     * @throws CTRException
     */
    private String readPdr() throws CTRException {
        log("Requesting IDENTIFICATION structure from device");
        String pdr = getRequestFactory().readIdentificationStructure().getPdr().getValue();
        if (pdr == null) {
            throw new CTRException("Unable to detect meter. PDR value was 'null'!");
        }
        return pdr;
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

            GPRSFrame response = getRequestFactory().getConnection().sendFrameGetResponse(readRequest);
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

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public Rtu getRtu() {
        return rtu;
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }
}

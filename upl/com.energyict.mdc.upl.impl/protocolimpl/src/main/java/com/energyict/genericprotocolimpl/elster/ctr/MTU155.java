package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.*;
import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureCtrConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationRequestStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationResponseStructure;
import com.energyict.protocolimpl.base.ProtocolProperties;
import com.energyict.protocolimpl.debug.DebugUtils;

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

    private ProtocolProperties properties = new MTU155Properties();
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
        // Hmmm, still work to do :)
    }

    public static void main(String[] args) throws IOException, LinkException, BusinessException, SQLException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException {
        int baudRate = 9600;
        int dataBits = SerialCommunicationChannel.DATABITS_8;
        int parity = SerialCommunicationChannel.PARITY_NONE;
        int stopBits = SerialCommunicationChannel.STOPBITS_1;

        Dialer dialer = DebugUtils.getConnectedDirectDialer("COM1", baudRate, dataBits, parity, stopBits);

        MTU155 mtu155 = new MTU155();
        mtu155.execute(null, dialer, Logger.getAnonymousLogger());
        CtrConnection connection = new SecureCtrConnection(dialer.getInputStream(), dialer.getOutputStream(), new MTU155Properties());



    }

    private IdentificationResponseStructure getIdentification() throws CTRConnectionException {
        if (identification == null) {
            GPRSFrame request = new GPRSFrame();
            request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
            request.getFunctionCode().setFunction(Function.IDENTIFICATION_REQUEST);
            request.getProfi().setLongFrame(false);
            request.getStructureCode().setStructureCode(StructureCode.IDENTIFICATION);
            request.setData(new IdentificationRequestStructure());
            request.setCpa(new Cpa(0x00));
            getConnection().sendFrameGetResponse(request);
        }
        return this.identification;
    }

    public CtrConnection getConnection() {
        return connection;
    }
}

package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureCtrConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationRequestStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.IdentificationResponseStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 12-okt-2010
 * Time: 11:04:01
 */
public class GprsRequestFactory {

    private final CtrConnection connection;
    private MTU155Properties properties;
    private Logger logger;

    public GprsRequestFactory(Link link, Logger logger, MTU155Properties properties) {
        this.connection = new SecureCtrConnection(link.getInputStream(), link.getOutputStream(), properties);
        this.logger = logger;
        this.properties = properties;
    }

    public GPRSFrame getIdentificationRequest() {
        GPRSFrame request = new GPRSFrame();
        request.setAddress(getAddress());
        request.getFunctionCode().setEncryptionStatus(EncryptionStatus.NO_ENCRYPTION);
        request.getFunctionCode().setFunction(Function.IDENTIFICATION_REQUEST);
        request.getProfi().setLongFrame(false);
        request.getStructureCode().setStructureCode(StructureCode.IDENTIFICATION);
        request.setData(new IdentificationRequestStructure());
        request.setCpa(new Cpa(0x00));
        return request;
    }

    public CtrConnection getConnection() {
        return connection;
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public MTU155Properties getProperties() {
        if (properties == null) {
            this.properties = new MTU155Properties();
        }
        return properties;
    }

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, CTRObjectID... objectId) {
        List<AbstractCTRObject> registerList = new ArrayList<AbstractCTRObject>();

        // request

        for (CTRObjectID objectID : objectId) {

        }
        return registerList;
    }


    /**
     * @return
     * @throws CTRException
     */
    public IdentificationResponseStructure readIdentificationStructure() throws CTRException {
        GPRSFrame response = getConnection().sendFrameGetResponse(getIdentificationRequest());
        if (response.getData() instanceof IdentificationResponseStructure) {
            return (IdentificationResponseStructure) response.getData();
        } else {
            throw new CTRException("Expected IdentificationResponseStructure but was " + response.getData().getClass().getSimpleName());
        }
    }

    public Address getAddress() {
        return new Address(getProperties().getAddress());
    }
}

package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.genericprotocolimpl.elster.ctr.*;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.info.SealStatusBit;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.AckStructure;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 14/03/11
 * Time: 16:14
 */
public class SealConfig {

    private static final String CURRENT_SEAL_STATUS = "D.9.0";
    private static final String BREAK_SEAL_STATUS = "11.0.6";
    private static final String RESTORE_SEAL_STATUS = "11.0.7";

    private static final int DEACTIVATE_TIME_GPRS = 1;
    private static final int DEACTIVATE_TIME_SMS = 48;  // An sms can be delayed up to 48 hours.

    private static final int MAX_BREAK_TIME = 255;
    private final RequestFactory factory;
    private int sealStatus = -1;

    public SealConfig(RequestFactory factory) {
        this.factory = factory;
    }

    public int getSealStatus() throws CTRException {
        if (sealStatus == -1) {
            List<AbstractCTRObject> objects = getFactory().queryRegisters(new AttributeType(0x03), CURRENT_SEAL_STATUS);
            if ((objects == null) || (objects.size() <= 0)) {
                throw new CTRException("Unable to read the previous seal status! List of objects returned was empty or null.");
            }
            CTRAbstractValue[] values = objects.get(0).getValue();
            if ((values == null) || (values.length <= 0)) {
                throw new CTRException("Unable to read the previous seal status! List of values returned was empty or null.");
            }
            sealStatus = values[0].getIntValue();
        }
        return sealStatus;
    }

    public RequestFactory getFactory() {
        return factory;
    }

    /**
     * @param statusBit
     * @throws CTRException
     */
    public void breakAndRestoreSeal(SealStatusBit statusBit) throws CTRException {
        validateSealStatusBit(statusBit);
        if (getFactory() instanceof SmsRequestFactory) {
            breakSealTemporary(statusBit);
        } else if (statusBit.isSealInteger(getSealStatus())) {
            breakSealTemporary(statusBit);
        }
    }

    /**
     * @param statusBit
     * @throws CTRException
     */
    public void breakSealTemporary(SealStatusBit statusBit) throws CTRException {
        breakSealTemporary(statusBit, (getFactory() instanceof GprsRequestFactory) ? DEACTIVATE_TIME_GPRS : DEACTIVATE_TIME_SMS);
    }

    /**
     * @param statusBit
     * @param breakForThisTime
     */
    public void breakSealTemporary(SealStatusBit statusBit, int breakForThisTime) throws CTRException {
        validateSealStatusBit(statusBit);
        validateBreakTime(breakForThisTime);
        Data data = getFactory().executeRequest(new CTRObjectID(BREAK_SEAL_STATUS), new byte[]{(byte) statusBit.getBitNumber(), (byte) breakForThisTime});
        if ((data != null) &&  !(data instanceof AckStructure)) {
            throw new CTRException("Unable to deactivateSealTemporary [" + statusBit + "]. Did not receive AckStructure but [" + data.getClass().getName() + "]");
        }
    }

    /**
     * @param statusBit
     * @throws CTRException
     */
    public void breakSealPermanent(SealStatusBit statusBit) throws CTRException {
        validateSealStatusBit(statusBit);
        Data data = getFactory().executeRequest(new CTRObjectID(BREAK_SEAL_STATUS), new byte[]{(byte) statusBit.getBitNumber(), 0});
        if ((data != null) &&  !(data instanceof AckStructure)) {
            throw new CTRException("Unable to deactivateSealPermanent [" + statusBit + "]. Did not receive AckStructure but [" + data.getClass().getName() + "]");
        }
    }


    /**
     * @param statusBit
     * @throws CTRException
     */
    public void restoreSeal(SealStatusBit statusBit) throws CTRException {
        validateSealStatusBit(statusBit);
        Data data = getFactory().executeRequest(new CTRObjectID(RESTORE_SEAL_STATUS), new byte[]{(byte) statusBit.getBitNumber()});
        if ((data != null) &&  !(data instanceof AckStructure)) {
            throw new CTRException("Unable to activateSeal [" + statusBit + "]. Did not receive AckStructure but [" + data.getClass().getName() + "]");
        }
    }

    /**
     *
     * @param breakTime
     * @throws CTRException
     */
    private void validateBreakTime(int breakTime) throws CTRException {
        if ((breakTime < 0) && (breakTime > MAX_BREAK_TIME)) {
            throw new CTRException("Temporary break a seal is only allowed for a time period between 0 and 255, but was [" + breakTime + "]");
        }
    }

    /**
     * Checks if a given sealStatusbit is valid or not. There are a lot of unused or reserved seal bits.
     *
     * @param bit
     * @throws CTRException
     */
    private void validateSealStatusBit(SealStatusBit bit) throws CTRException {
        if (!bit.isRealStatusBit()) {
            throw new CTRException("SealStatusBit [" + bit + "] is not used in the MTU! Cannot change seal status.");
        }
    }

}

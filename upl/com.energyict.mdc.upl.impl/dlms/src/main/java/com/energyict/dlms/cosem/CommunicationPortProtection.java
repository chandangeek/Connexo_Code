package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.CommunicationPortProtectionAttributes;
import com.energyict.dlms.cosem.methods.CommunicationPortProtectionMethods;

import java.io.IOException;

public class CommunicationPortProtection extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public CommunicationPortProtection(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.COMMUNICATION_PORT_PROTECTION.getClassId();
    }

    public AbstractDataType readLogicalName() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.LOGICAL_NAME, OctetString.class);
    }

    /**
     * Controls the protection mode.
     * (1) permanently_locked,
     * (2) locked_on_failed_attempts,
     * (3) permanently_unlocked
     * @return
     * @throws IOException
     */
    public AbstractDataType readProtectionMode() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.PROTECTION_MODE, TypeEnum.class);
    }

    /**
     * Holds the number of allowed failed communication attempts before the lockout mechanism is triggered.
     * @return
     * @throws IOException
     */
    public AbstractDataType readAllowedFailedAttempts() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.ALLOWED_FAILED_ATTEMPTS, Unsigned16.class);
    }

    /**
     * Holds the initial value of the lockout time, in seconds,
     * after the first failed communication attempt when allowed_failed_attempts is reached.
     * @return
     * @throws IOException
     */
    public AbstractDataType readInitialLockoutTime() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.INITIAL_LOCKOUT_TIME, Unsigned32.class);
    }

    /**
     * Holds a factor that controls how the lockout time is increased with each failed attempt,
     * until the max_lockout_time is reached.
     * @return
     * @throws IOException
     */
    public AbstractDataType readSteepnessFactor() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.STEEPNESS_FACTOR, Unsigned8.class);
    }

    /**
     * Holds the maximum time, in seconds, for which the communication port can be locked,
     * even if the number of failed attempts keeps increasing.
     * The purpose of this attribute is to avoid a denial of service attack.
     * @return
     * @throws IOException
     */
    public AbstractDataType readMaxLockoutTime() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.MAX_LOCKOUT_TIME, Unsigned32.class);
    }

    /**
     * Contains the logical name of a communication port setup object related to the communication port being protected.
     * @return
     * @throws IOException
     */
    public AbstractDataType readPortReference() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.PORT_REFERENCE, OctetString.class);
    }

    /**
     * Holds the current protection status of the communication port.
     * (1) unlocked,
     * (2) temporarily_locked,
     * (3) permanently_locked
     * @return
     * @throws IOException
     */
    public AbstractDataType readProtectionStatus() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.PROTECTION_STATUS, TypeEnum.class);
    }

    /**
     * Hold the number of failed attempts since the last reset.
     * @return
     * @throws IOException
     */
    public AbstractDataType readFailedAttempts() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.FAILED_ATTEMPTS, Unsigned32.class);
    }

    /**
     * Holds the cumulative number of failed attempts,
     * independently of the protection_mode and the triggering of the protection mechanism.
     * This attribute is never reset.
     * @return
     * @throws IOException
     */
    public AbstractDataType readCumulativeFailedAttempts() throws IOException {
        return readDataType(CommunicationPortProtectionAttributes.CUMULATIVE_FAILED_ATTEMPTS, Unsigned32.class);
    }

    public void writeAttribute(CommunicationPortProtectionAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

    public void invokeMethod(CommunicationPortProtectionMethods method, AbstractDataType data) throws IOException {
        methodInvoke(method, data);
    }

}

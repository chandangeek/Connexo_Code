package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.attributes.ChangeOfTenancyOrSupplierManagementAttributes;
import com.energyict.dlms.cosem.methods.ChangeOfTenancyOrSupplierManagementMethods;

import java.io.IOException;

/**
 * Contains functionality to adjust/handle the ChangeOfTenantOrSupplier Management object of GB Smart Enhanced Credit
 */
public class ChangeOfTenancyOrSupplierManagement extends AbstractCosemObject {

    public static final byte[] LN = new byte[]{0, 0, 65, 0, 0, (byte) 255};

    private AXDRDateTime startTime;
    private Array scriptExecuted;
    private OctetString tenantReference;
    private Unsigned16 tenantId;
    private OctetString supplierReference;
    private Unsigned16 supplierId;

    private AXDRDateTime passiveStartTime;
    private Array passiveScriptExecuted;
    private OctetString passiveTenantReference;
    private Unsigned16 passiveTenantId;
    private OctetString passiveSupplierReference;
    private Unsigned16 passiveSupplierId;

    private AXDRDateTime activationDate;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     */
    public ChangeOfTenancyOrSupplierManagement(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN));
    }

    public ChangeOfTenancyOrSupplierManagement(final ProtocolLink protocolLink, final ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * Getter for the dlms class id
     *
     * @return the id of the dlms class
     */
    @Override
    protected int getClassId() {
        return DLMSClassId.CHANGE_OF_TENANT_SUPPLIER_MANAGEMENT.getClassId();
    }

    public AXDRDateTime readStartTime() throws IOException {
        this.startTime = new AXDRDateTime(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.START_TIME));
        return this.startTime;
    }

    public AXDRDateTime getStartTime() throws IOException {
        if (startTime == null) {
            this.startTime = readStartTime();
        }
        return startTime;
    }

    public Array readScriptExecuted() throws IOException {
        this.scriptExecuted = new Array(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.SCRIPT_EXECUTED), 0, 0);
        return this.scriptExecuted;
    }

    public Array getScriptExecuted() throws IOException {
        if (scriptExecuted == null) {
            this.scriptExecuted = readScriptExecuted();
        }
        return scriptExecuted;
    }

    public OctetString readTenantReference() throws IOException {
        this.tenantReference = new OctetString(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.TENANT_REFERENCE));
        return this.tenantReference;
    }

    public OctetString getTenantReference() throws IOException {
        if (tenantReference == null) {
            this.tenantReference = readTenantReference();
        }
        return tenantReference;
    }

    public Unsigned16 readTenantId() throws IOException {
        this.tenantId = new Unsigned16(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.TENANT_ID), 0);
        return this.tenantId;
    }

    public Unsigned16 getTenantId() throws IOException {
        if (tenantId == null) {
            this.tenantId = readTenantId();
        }
        return tenantId;
    }

    public OctetString readSupplierReference() throws IOException {
        this.supplierReference = new OctetString(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.SUPPLIER_REFERENCE));
        return this.supplierReference;
    }

    public OctetString getSupplierReference() throws IOException {
        if (supplierReference == null) {
            this.supplierReference = readSupplierReference();
        }
        return supplierReference;
    }

    public Unsigned16 readSupplierId() throws IOException {
        this.supplierId = new Unsigned16(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.SUPPLIER_ID), 0);
        return this.supplierId;
    }

    public Unsigned16 getSupplierId() throws IOException {
        if (supplierId == null) {
            this.supplierId = readSupplierId();
        }
        return supplierId;
    }

    public void writePassiveStartTime(AXDRDateTime passiveStartTime) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.PASSIVE_START_TIME, passiveStartTime.getBEREncodedByteArray());
        this.passiveStartTime = passiveStartTime;
    }

    public void writePassiveScriptExecuted(Array passiveScriptExecuted) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.PASSIVE_SCRIPT_EXECUTED, passiveScriptExecuted.getBEREncodedByteArray());
        this.passiveScriptExecuted = passiveScriptExecuted;
    }

    public void writePassiveTenantReference(OctetString passiveTenantReference) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.PASSIVE_TENANT_REFERENCE, passiveTenantReference.getBEREncodedByteArray());
        this.passiveTenantReference = passiveTenantReference;
    }

    public void writePassiveTenantId(Unsigned16 passiveTenantId) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.PASSIVE_TENANT_ID, passiveTenantId.getBEREncodedByteArray());
        this.passiveTenantId = passiveTenantId;
    }

    public void writePassiveSupplierReference(OctetString passiveSupplierReference) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.PASSIVE_SUPPLIER_REFERENCE, passiveSupplierReference.getBEREncodedByteArray());
        this.passiveSupplierReference = passiveSupplierReference;
    }

    public void writePassiveSupplierId(Unsigned16 passiveSupplierId) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.PASSIVE_SUPPLIER_ID, passiveSupplierId.getBEREncodedByteArray());
        this.passiveSupplierId = passiveSupplierId;
    }


    public AXDRDateTime readActivationDate() throws IOException {
        this.activationDate = new AXDRDateTime(getResponseData(ChangeOfTenancyOrSupplierManagementAttributes.ACTIVATION_TIME));
        return this.activationDate;
    }


    public AXDRDateTime getActivationDate() throws IOException {
        if (this.activationDate == null) {
            readActivationDate();
        }
        return activationDate;
    }

    public void writeActivationDate(final AXDRDateTime activationDate) throws IOException {
        write(ChangeOfTenancyOrSupplierManagementAttributes.ACTIVATION_TIME, activationDate.getBEREncodedByteArray());
        this.activationDate = activationDate;
    }

    public byte[] reset() throws IOException {
        return methodInvoke(ChangeOfTenancyOrSupplierManagementMethods.RESET, new Integer8(0));
    }

    public byte[] activate() throws IOException {
        return methodInvoke(ChangeOfTenancyOrSupplierManagementMethods.ACTIVATE, new Integer8(0));
    }
}

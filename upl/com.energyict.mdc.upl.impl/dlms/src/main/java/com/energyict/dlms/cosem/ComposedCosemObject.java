package com.energyict.dlms.cosem;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 30-dec-2010
 * Time: 9:21:41
 */
public class ComposedCosemObject extends AbstractCosemObject implements Iterable<DLMSAttribute> {

    private final DLMSAttribute[] attributes;
    private final AbstractDataType[] dataResult;

    @Override
    protected int getClassId() {
        return getObjectReference().getClassId();
    }

    public ComposedCosemObject(ProtocolLink protocolLink, List<DLMSAttribute> dlmsAttributes) {
        this(protocolLink, dlmsAttributes.toArray(new DLMSAttribute[0]));
    }

    public ComposedCosemObject(ProtocolLink protocolLink, DLMSAttribute... dlmsAttributes) {
        super(protocolLink, new ObjectReference(ObisCode.fromString("0.0.0.0.0.0").getLN()));
        this.attributes = dlmsAttributes.clone();
        this.dataResult = new AbstractDataType[attributes.length];
    }

    public AbstractDataType getAttribute(DLMSAttribute attribute) throws IOException {
        int index = getAttributeIndex(attribute);
        readAttribute(attribute);
        if (dataResult[index] != null) {
            if (dataResult[index] instanceof DataAccessResultType) {
                throw new DataAccessResultException(dataResult[index].intValue());
            } else {
                return dataResult[index];
            }
        } else {
            throw new IOException("Unable to read attribute [" + attribute + "]. Expected DataAccessResult or DataType but Value is still null.");
        }
    }

    private boolean isGetWithListSupported() {
        ApplicationServiceObject serviceObject = getProtocolLink().getDLMSConnection().getApplicationServiceObject();
        if (serviceObject != null) {
            ConformanceBlock block = serviceObject.getAssociationControlServiceElement().getXdlmsAse().getNegotiatedConformanceBlock();
            if (block != null) {
                return block.isMultipleReferences();
            }
        }
        return false;
    }

    private int getAttributeIndex(DLMSAttribute attribute) throws IOException {
        for (int i = 0; i < attributes.length; i++) {
            DLMSAttribute dlmsAttribute = attributes[i];
            if (dlmsAttribute.equals(attribute)) {
                return i;
            }
        }
        throw new IOException("ComposedCosemObject does not contain attribute ["+attribute+"].");
    }

    private void readAttribute(DLMSAttribute attribute) throws IOException {
        int index = getAttributeIndex(attribute);
        if (dataResult[index] == null) {
            if (isGetWithListSupported()) {
                byte[][] dataWithList = getLNResponseDataWithList(attributes);
                for (int i = 0; i < dataWithList.length; i++) {
                    switch (dataWithList[i][0]) {
                        case 0x00: // Data
                            dataResult[i] = AXDRDecoder.decode(dataWithList[i], 1);
                            break;
                        case 0x01: // Data-access-result
                            dataResult[i] = new DataAccessResultType(DataAccessResultCode.byResultCode(dataWithList[i][1] & 0x0FF));
                            break;
                        default:
                            throw new IOException("Invalid response while reading GetResponseWithList: expected '0' or '1' but was " + dataWithList[i][0]);
                    }
                }
            } else {
                try {
                    setObjectReference(new ObjectReference(attribute.getObisCode().getLN(), attribute.getClassId()));
                    byte[] reponseData = getLNResponseData(attribute.getAttribute());
                    AbstractDataType abstractData = AXDRDecoder.decode(reponseData);
                    dataResult[index] = abstractData;
                } catch (DataAccessResultException e) {
                    dataResult[index] = new DataAccessResultType(e.getCode());
                }
            }
        }
    }

    public boolean contains(DLMSAttribute attribute) {
        for (int i = 0; i < attributes.length; i++) {
            DLMSAttribute dlmsAttribute = attributes[i];
            if (dlmsAttribute.equals(attribute)) {
                return true;
            }
        }
        return false;
    }

    public long getValue() throws IOException {
        return 0;
    }

    public Date getCaptureTime() throws IOException {
        return new Date(0);
    }

    public ScalerUnit getScalerUnit() throws IOException {
        return new ScalerUnit(0, Unit.getUndefined());
    }

    public Quantity getQuantityValue() throws IOException {
        return new Quantity(0, Unit.getUndefined());
    }

    public Date getBillingDate() throws IOException {
        return new Date(0);
    }

    public int getResetCounter() {
        return 0;
    }

    public String getText() throws IOException {
        return toString();
    }

    @Override
    public String toString() {
        String crlf = "\r\n";
        final StringBuilder sb = new StringBuilder();
        sb.append("ComposedCosemObject").append(crlf);
        for (int i = 0; i < attributes.length; i++) {
            DLMSAttribute attribute = attributes[i];
            sb.append("  [").append(ProtocolTools.addPadding(String.valueOf(i), '0', 2, false)).append("] ").append(attribute).append(crlf);
        }
        sb.append('}');
        return sb.toString();
    }

    public Iterator iterator() {
        return Arrays.asList(attributes).iterator();
    }
}

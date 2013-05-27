package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 10/24/12
 * Time: 3:41 PM
 */
public class QualityOfService extends Structure {

    public QualityOfService(final byte[] berEncodedData) throws IOException {
        super(berEncodedData, 0, 0);
    }

    public final QualityOfServiceElement getDefault() throws IOException {
        final Structure defaultStructure = getDataType(0, Structure.class);
        final QualityOfServiceElement defaultQos = QualityOfServiceElement.fromStructure(defaultStructure);
        return defaultQos;
    }

    public final QualityOfServiceElement getRequested() throws IOException {
        final Structure requestedStructure = getDataType(1, Structure.class);
        final QualityOfServiceElement requestedQos = QualityOfServiceElement.fromStructure(requestedStructure);
        return requestedQos;
    }

    public static final QualityOfService fromStructure(final Structure structure) throws IOException {
        return new QualityOfService(structure.getBEREncodedByteArray());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        QualityOfServiceElement defaultQos;
        try {
            defaultQos = getDefault();
        } catch (IOException e) {
            e.printStackTrace();
            defaultQos = null;
        }

        QualityOfServiceElement requestedQos;
        try {
            requestedQos = getRequested();
        } catch (IOException e) {
            e.printStackTrace();
            requestedQos = null;
        }

        sb.append("def=[").append(defaultQos == null ? '-' : defaultQos.toString()).append("], ");
        sb.append("req=[").append(requestedQos == null ? '-' : requestedQos.toString()).append(']');

        return sb.toString();
    }
}

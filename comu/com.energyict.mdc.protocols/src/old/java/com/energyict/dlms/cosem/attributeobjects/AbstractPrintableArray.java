package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;

import java.io.IOException;

/**
 * @author jme
 */
public abstract class AbstractPrintableArray<T extends AbstractDataType> extends Array {

    protected abstract T getArrayItem(int itemNumber);

    public AbstractPrintableArray(byte[] berEncodedData, int offset, int level) throws IOException {
        super(berEncodedData, offset, level);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nrOfDataTypes(); i++) {
            sb.append("[").append(i).append("](");
            sb.append(getArrayItem(i));
            sb.append(")");
            if (i != (nrOfDataTypes() - 1)) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}

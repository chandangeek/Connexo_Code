package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 15:01:20
 */
public class CTRObjectFactory {

    public AbstractCTRObject parse(byte[] rawData, int offset) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();
        CTRObjectID id = parser.parseId(rawData, offset);
        int[] valueLength = parser.parseValueLength(id);
        return this.createObject(id, rawData, offset, valueLength);
    }
    
    private AbstractCTRObject createObject(CTRObjectID id, byte[] rawData, int offset, int[] valueLength) {
 
        AbstractCTRObject obj = null;

        if (id.getX() == 0x01) {       //Category is Flow/Volume
            obj = createFlowAndVolumeObject(id, rawData, offset, valueLength, obj);
        }
        
        return obj;
    }

    private AbstractCTRObject createFlowAndVolumeObject(CTRObjectID id, byte[] rawData, int offset, int[] valueLength, AbstractCTRObject obj) {
        obj = new FlowAndVolumeCategory(id);
        obj.parse(rawData, offset, valueLength);
        return obj;
    }
}

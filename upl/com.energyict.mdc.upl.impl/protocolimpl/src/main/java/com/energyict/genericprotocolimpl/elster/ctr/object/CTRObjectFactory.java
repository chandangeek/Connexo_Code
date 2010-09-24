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
        return this.createObject(id, rawData, offset);
    }
    
    private AbstractCTRObject createObject(CTRObjectID id, byte[] rawData, int offset) {
 
        AbstractCTRObject obj = null;
        int x = id.getX();
        switch (x) {
            case 1: obj = createFlowAndVolumeObject(id, rawData, offset, obj);
            case 2: obj = createTotalizerObject(id, rawData, offset, obj);
            case 3: //....

        }

        return obj;
    }

    private AbstractCTRObject createFlowAndVolumeObject(CTRObjectID id, byte[] rawData, int offset, AbstractCTRObject obj) {
        obj = new FlowAndVolumeCategory(id);
        obj.parse(rawData, offset);
        return obj;
    }

    private AbstractCTRObject createTotalizerObject(CTRObjectID id, byte[] rawData, int offset, AbstractCTRObject obj) {
        obj = new TotalizersCategory(id);
        obj.parse(rawData, offset);
        return obj;
    }


}

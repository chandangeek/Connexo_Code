package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class FlowAndVolumeCategory extends AbstractSimpleBINObject{

    public FlowAndVolumeCategory(CTRObjectID id) {
        this.setId(id);
    }

    //Parse the raw data & fill in the object's properties
    @Override
    public void parse(byte[] rawData, int offset) {

        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static
        CTRObjectID id = this.getId();
        offset +=2; //Skip the Id bytes

        this.setQlf(parser.parseQlf(rawData, offset));
        offset +=1;
        
        int[] valueLength = this.parseValueLengths(id);

        this.setValue(parser.parseBINValue(id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset +=1;

        this.setDefault(null);

        this.setSymbol(parser.parseSymbol(id));
    }

    private int[] parseValueLengths(CTRObjectID id) {
        int[] valueLength;
        switch(id.getY()) {
                default: valueLength = new int[]{3}; break;
                case 6:
                case 7:
                case 9:
                case 0x0A:
                    valueLength = new int[]{3,1,1};
                    switch (id.getZ()) {
                        case 4: valueLength = new int[]{3,1,1,1}; break;
                        case 5:
                        case 6: valueLength = new int[]{3,1,1,1,1}; break;
                    }
            }
        return valueLength;  //To change body of created methods use File | Settings | File Templates.
    }

    private int sum(int[] valueLength) {
        int sum = 0;
        for(int i:valueLength) {sum +=i;}
        return sum;
    }

}

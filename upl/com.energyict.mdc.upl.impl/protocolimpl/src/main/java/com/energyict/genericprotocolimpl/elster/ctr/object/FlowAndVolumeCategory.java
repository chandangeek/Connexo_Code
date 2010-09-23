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
    public void parse(byte[] rawData, int offset, int[] valueLength) {

        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static
        CTRObjectID id = this.getId();
        offset +=2; //Skip the Id bytes

        this.setQlf(parser.parseQlf(rawData, offset));
        offset +=1;

        this.setValue(parser.parseBINValue(id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset +=1;

        this.setDefault(null);

        this.setSymbol(parser.parseSymbol(id));
    }

    private int sum(int[] valueLength) {
        int sum = 0;
        for(int i:valueLength) {sum +=i;}
        return sum;
    }

}

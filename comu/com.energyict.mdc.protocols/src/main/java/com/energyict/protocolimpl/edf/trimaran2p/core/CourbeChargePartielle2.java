/*
 * CourbeChargePartielle.java
 *
 * Created on 21 februari 2007, 13:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class CourbeChargePartielle2 extends AbstractTrimaranObject {

    final int DEBUG=0;

    private int[] values;

    /** Creates a new instance of TemplateVariableName */
    public CourbeChargePartielle2(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CourbeChargePartielle2:\n");
        for (int i=0;i<getValues().length;i++) {
            strBuff.append("       values["+i+"]=0x"+Integer.toHexString(getValues()[i])+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 408;
    }

    protected byte[] prepareBuild() throws IOException {
        return null;
    }

    protected void parse(byte[] data) throws IOException {

//        System.out.println("GN_DEBUG> write to file");
//        File file = new File("c://TEST_FILES/CourbeCharge_par2.bin");
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(data);
//        fos.close();

        int offset=0;
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());

        if (DEBUG>=1) System.out.println("CourbeChargePartielle2, parse, "+ProtocolUtils.outputHexString(data));
        if (DEBUG>=1) System.out.println("CourbeChargePartielle2, parse, dc.getRoot().getNrOfElements()="+dc.getRoot().getNrOfElements());
        if (DEBUG>=1) System.out.println("CourbeChargePartielle2, parse, "+dc.print2strDataContainer());

        setValues(new int[dc.getRoot().getNrOfElements()]);
        for (int i=0;i<getValues().length;i++) {
            getValues()[i] = dc.getRoot().getInteger(i) & 0xffff;
        }
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }

}

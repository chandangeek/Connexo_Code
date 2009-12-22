/*
 * PLCCTemplateObject.java
 *
 * Created on 3 december 2007, 13:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;



import java.io.IOException;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.obis.ObisCode;

/**
 *
 * @author kvds
 */
public class PLCCTemplateObject extends AbstractPLCCObject {

    /** Creates a new instance of PLCCTemplateObject */
    public PLCCTemplateObject(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.0.0.0.0"), DLMSClassId.DATA.getClassId());
    }

    public PLCCTemplateObject() {
        super(null);
    }

//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new PLCCTemplateObject()));
//    }

    protected void doInvoke() throws IOException {

    }
}

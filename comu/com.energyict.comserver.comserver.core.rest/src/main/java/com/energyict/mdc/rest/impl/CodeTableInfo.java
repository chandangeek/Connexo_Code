package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.impl.properties.MdcResourceProperty;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.coreimpl.CodeFactoryImpl;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
@XmlRootElement
public class CodeTableInfo implements MdcResourceProperty {

    public int codeTableId;
    public String name;

    public CodeTableInfo(Code codeTable) {
        codeTableId = codeTable.getId();
        name = codeTable.getName();
    }

    @Override
    public Object fromResourceObject() {
        return new CodeFactoryImpl().find(codeTableId);
    }
}

package com.energyict.mdc.rest.impl.properties.propertycontexts;

import com.energyict.mdc.rest.impl.properties.PropertyContext;
import com.energyict.mdw.core.Code;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
@XmlRootElement
public class CodeTableInfo implements PropertyContext {

    public int codeTableId;
    public String name;

    public CodeTableInfo(Code codeTable) {
        codeTableId = codeTable.getId();
        name = codeTable.getName();
    }
}

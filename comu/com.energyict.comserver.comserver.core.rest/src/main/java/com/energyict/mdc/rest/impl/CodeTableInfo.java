package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.impl.properties.MdcResourceProperty;
import com.energyict.mdc.protocol.api.codetables.Code;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * Represents the Info object for a {@link Code}
 *
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
@XmlRootElement
public class CodeTableInfo implements MdcResourceProperty {

    public int codeTableId;
    public String name;

    public CodeTableInfo() {
    }

    public CodeTableInfo(Map<String, Object> map) {
        this.codeTableId = (int) map.get("codeTableId");
        this.name = (String) map.get("name");
    }

    public CodeTableInfo(Code codeTable) {
        codeTableId = codeTable.getId();
        name = codeTable.getName();
    }

}

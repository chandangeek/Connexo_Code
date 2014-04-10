package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.codetables.Code;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the Info object for a {@link Code}
 *
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:08
 */
@XmlRootElement
public class CodeTableInfo {

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

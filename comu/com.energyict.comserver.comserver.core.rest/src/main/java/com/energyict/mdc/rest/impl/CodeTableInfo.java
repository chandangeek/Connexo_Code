package com.energyict.mdc.rest.impl;

import com.energyict.mdw.core.Code;

import javax.xml.bind.annotation.XmlRootElement;

/**
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

    public CodeTableInfo(Code codeTable) {
        codeTableId = codeTable.getId();
        name = codeTable.getName();
    }
}

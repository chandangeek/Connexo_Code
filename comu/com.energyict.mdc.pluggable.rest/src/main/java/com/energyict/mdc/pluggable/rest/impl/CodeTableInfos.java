package com.energyict.mdc.pluggable.rest.impl;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 21/11/13
 * Time: 15:14
 */
@XmlRootElement
public class CodeTableInfos {

    @XmlElement
    @XmlElementWrapper(name = "Code")
    public Collection<? super CodeTableInfo> codeTableInfos = new HashSet<>();
}

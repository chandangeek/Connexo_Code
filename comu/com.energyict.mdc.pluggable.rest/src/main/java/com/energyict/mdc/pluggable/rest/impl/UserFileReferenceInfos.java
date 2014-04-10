package com.energyict.mdc.pluggable.rest.impl;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserFileReferenceInfos {

    @XmlElement
    @XmlElementWrapper(name = "UserFile")
    public Collection<? super UserFileReferenceInfo> userFileReferenceInfos = new HashSet<>();
}

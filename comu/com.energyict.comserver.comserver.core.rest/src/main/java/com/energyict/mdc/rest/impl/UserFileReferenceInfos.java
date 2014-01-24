package com.energyict.mdc.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

@XmlRootElement
public class UserFileReferenceInfos {

    @XmlElement
    @XmlElementWrapper(name = "UserFile")
    public Collection<? super UserFileReferenceInfo> userFileReferenceInfos = new HashSet<>();
}

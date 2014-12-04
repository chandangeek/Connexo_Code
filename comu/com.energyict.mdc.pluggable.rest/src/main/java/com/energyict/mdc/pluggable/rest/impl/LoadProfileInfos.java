package com.energyict.mdc.pluggable.rest.impl;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoadProfileInfos {


    @XmlElement
    @XmlElementWrapper(name = "LoadProfile")
    public Collection<? super LoadProfileInfo> loadProfileInfos = new HashSet<>();
}

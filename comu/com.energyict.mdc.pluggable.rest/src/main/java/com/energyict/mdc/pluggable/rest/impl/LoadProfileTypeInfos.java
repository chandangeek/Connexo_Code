package com.energyict.mdc.pluggable.rest.impl;

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoadProfileTypeInfos {


    @XmlElement
    @XmlElementWrapper(name = "LoadProfileType")
    public Collection<? super LoadProfileTypeInfo> loadProfileTypeInfos = new HashSet<>();
}

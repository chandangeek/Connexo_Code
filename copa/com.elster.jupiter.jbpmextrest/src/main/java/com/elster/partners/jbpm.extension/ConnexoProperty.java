package com.elster.partners.jbpm.extension;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConnexoProperty {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String value;
}

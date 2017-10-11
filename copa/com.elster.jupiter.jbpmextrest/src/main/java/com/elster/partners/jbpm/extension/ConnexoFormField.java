/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.List;


@XmlType(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(ConnexoProperty.class)
public class ConnexoFormField {

    @XmlAttribute
    public String position;

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String type;

    @XmlAttribute
    public String id;

    @XmlElement(name = "property")
    public List<ConnexoProperty> properties;
}

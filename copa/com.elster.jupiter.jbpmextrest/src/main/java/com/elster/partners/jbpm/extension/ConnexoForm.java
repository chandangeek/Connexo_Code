/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import org.kie.api.task.model.Status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.List;
import java.util.Map;


@XmlRootElement(name = "form")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ConnexoFormField.class, ConnexoProperty.class})
public class ConnexoForm {

    @XmlAttribute
    public String id;

    @XmlElement(name = "property")
    public List<ConnexoProperty> properties;

    @XmlElement(name = "field")
    public List<ConnexoFormField> fields;

    public Map<String, Object> content;

    public Map<String, Object> outContent;

    public Status taskStatus;
}

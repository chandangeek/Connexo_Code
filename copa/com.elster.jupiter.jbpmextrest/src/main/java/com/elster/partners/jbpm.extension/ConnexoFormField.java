package com.elster.partners.jbpm.extension;

import javax.xml.bind.annotation.*;
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

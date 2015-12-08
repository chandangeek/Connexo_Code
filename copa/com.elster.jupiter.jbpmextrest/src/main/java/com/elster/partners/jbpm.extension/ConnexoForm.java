package com.elster.partners.jbpm.extension;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.kie.api.task.model.Status;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
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

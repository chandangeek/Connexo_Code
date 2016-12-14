package com.energyict.mdc.upl.meterdata.identifiers;

import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * Models a unique identification mechanism.
 * An Identifier can be serialized with the standard
 * java serialization mechanism but can also be serialized to JSON.
 * The JSON serialization has been setup such that it can be deserialized
 * to the same class (to avoid the loss of behavior of cours)
 * so most of the typing information is available in JSON.
 * However, to protect clients that depend on typing information
 * from renaming of implementation classes, an introspection
 * mechanism was introduced.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-14 (11:01)
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public interface Identifier extends Serializable {

    Introspector forIntrospection();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    default String getXmlType() {
        return getClass().getName();
    }

    default void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

}
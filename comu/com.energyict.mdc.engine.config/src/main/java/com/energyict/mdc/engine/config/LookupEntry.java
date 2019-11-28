package com.energyict.mdc.engine.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlElement;

/**
 * represents an entry in a lookup table
 *
 * @author Geert
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.CLASS,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
public interface LookupEntry  extends HasId, HasName {

    /**
     * Returns the value
     *
     * @return the value
     */
    public String getValue();

    /**
     * Returns the key
     *
     * @return the key
     */
    public int getKey();

    /**
     * Returns the id of the Lookup object
     * this entry belongs to
     *
     * @return the lookup id.
     */
    public int getLookupId();

    @ProviderType
    interface LookupEntryBuilder<CS extends LookupEntry, CSB extends LookupEntryBuilder> {
        CSB key(String key);
        CSB value(String value);
        CSB active(boolean active);
        CS create();
    }

    void update();

    /**
     * Returns the receiver's custom translated value (or simply the value itself if no translation is available)
     *
     * @return the receiver's custom translated value
     */
    public String getCustomValue();

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    public String getXmlType();

    public void setXmlType(String ignore);
}
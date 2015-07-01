package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:17
 */
@XmlRootElement
public class RTU3Schedule {

    public long id;
    public String name;
    public String specification;

    public RTU3Schedule(long id, String name, String specification) {
        this.id = id;
        this.name = name;
        this.specification = specification;
    }

    //JSon constructor
    private RTU3Schedule() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getId()));
        structure.addDataType(OctetString.fromString(getName()));
        structure.addDataType(OctetString.fromString(getSpecification()));
        return structure;
    }

    @XmlAttribute
    public long getId() {
        return id;
    }

    @XmlAttribute
    public String getName() {
        return name;
    }

    @XmlAttribute
    public String getSpecification() {
        return specification;
    }
}
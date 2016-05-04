package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:17
 */
@XmlRootElement
public class Beacon3100Schedule {

    public long id;
    public String name;
    public String specification;

    public Beacon3100Schedule(long id, String name, String specification) {
        this.id = id;
        this.name = name;
        this.specification = specification;
    }

    //JSon constructor
    private Beacon3100Schedule() {
    }

    public boolean equals(AbstractDataType anotherScheduleStructure){
        try {
            byte[] otherByteArray = anotherScheduleStructure.getBEREncodedByteArray();
            byte[] thisByteArray = toStructure().getBEREncodedByteArray();

            return Arrays.equals(thisByteArray, otherByteArray);
        }catch (Exception ex){
            return false;
        }
    }

    public boolean equals(Beacon3100Schedule anotherSchedule){
        return this.equals(anotherSchedule.toStructure());
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
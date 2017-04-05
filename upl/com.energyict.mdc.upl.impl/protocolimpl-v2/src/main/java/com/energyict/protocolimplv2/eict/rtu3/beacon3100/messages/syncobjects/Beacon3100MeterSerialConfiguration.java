package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:09
 */
@XmlRootElement
public class Beacon3100MeterSerialConfiguration {

	/**
	 * Create a new {@link Beacon3100MeterSerialConfiguration} based on the {@link Structure} received from the device.
	 * 
	 * @param 		structure		The structure received from the device.
	 * 
	 * @return		The parsed {@link Beacon3100MeterSerialConfiguration}.
	 * 
	 * @throws		IOException		If an error occurs parsing the data.
	 */
	public static final Beacon3100MeterSerialConfiguration fromStructure(final Structure structure) throws IOException {
		final ObisCode serialNumberObis = ObisCode.fromByteArray(structure.getDataType(0, OctetString.class).getOctetStr());
		final int clientType = structure.getDataType(1).getUnsigned32().intValue();
		
		return new Beacon3100MeterSerialConfiguration(serialNumberObis, clientType);		
	}
	
    private ObisCode serialNumberObisCode;
    private int clientTypeId;

    public Beacon3100MeterSerialConfiguration(ObisCode serialNumberObisCode, int clientTypeId) {
        this.serialNumberObisCode = serialNumberObisCode;
        this.clientTypeId = clientTypeId;
    }

    //JSon constructor
    private Beacon3100MeterSerialConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromObisCode(getSerialNumberObisCode()));
        structure.addDataType(new Unsigned32(getClientTypeId()));
        return structure;
    }

    @XmlAttribute
    public ObisCode getSerialNumberObisCode() {
        return serialNumberObisCode;
    }

    @XmlAttribute
    public int getClientTypeId() {
        return clientTypeId;
    }
}
package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:29
 */
@XmlRootElement
public class Beacon3100Schedulable {

	/**
	 * Parses a {@link Beacon3100Schedulable} from the given {@link Structure}.
	 *
	 * @param 		structure		The {@link Structure} to parse.
	 *
	 * @return		The parsed {@link Beacon3100Schedulable}.
	 *
	 * @throws 		IOException		If an IO error occurs.
	 */
	public static final Beacon3100Schedulable fromStructure(final Structure structure) throws IOException {
		final long scheduleId = structure.getDataType(0, Unsigned32.class).longValue();
		final int logicalDeviceId = structure.getDataType(1, Unsigned16.class).intValue();
		final int clientTypeId = structure.getDataType(2, Unsigned32.class).intValue();
		final List<SchedulableItem> profiles = toSchedulableItems(structure.getDataType(3, Array.class));
		final List<SchedulableItem> registers = toSchedulableItems(structure.getDataType(4,  Array.class));
		final List<SchedulableItem> eventLogs = toSchedulableItems(structure.getDataType(5, Array.class));

		return new Beacon3100Schedulable(0L, scheduleId, logicalDeviceId, clientTypeId, profiles, registers, eventLogs);
	}

	/**
	 * Converts the given {@link Array} into a {@link List} of {@link SchedulableItem}s.
	 *
	 * @param 		items		The {@link Array} to convert.
	 *
	 * @return		The {@link List} of {@link SchedulableItem}s.
	 *
	 * @throws 		IOException		If an error occurs while parsing the data.
	 */
	private static final List<SchedulableItem> toSchedulableItems(final Array items) throws IOException {
		final List<SchedulableItem> schedulableItems = new ArrayList<>();

		for (final AbstractDataType dataType : items) {
			if (dataType.isOctetString()) {
				final ObisCode logicalName = ObisCode.fromByteArray(dataType.getOctetString().getContentByteArray());

				schedulableItems.add(new SchedulableItem(logicalName, new Unsigned16(1)));
			} else if (dataType.isStructure()) {
				final Structure structure = dataType.getStructure();

				schedulableItems.add(SchedulableItem.fromStructure(structure));
			}
		}

		return schedulableItems;
	}


    private long scheduleId;
    private int logicalDeviceId;
    private long clientTypeId;
    private List<SchedulableItem> profiles;
    private List<SchedulableItem> registers;
    private List<SchedulableItem> eventLogs;
    boolean readOldObisCodes;


    /**
     * A reference to the original comTaskEnablement that defined this masterdata
     */
    private long originalId;

    public Beacon3100Schedulable(long originalId, long scheduleId, int logicalDeviceId, long clientTypeId, List<SchedulableItem> profiles, List<SchedulableItem> registers, List<SchedulableItem> eventLogs) {
        this(originalId, scheduleId, logicalDeviceId, clientTypeId, profiles, registers, eventLogs, true);
    }

    public Beacon3100Schedulable(long originalId, long scheduleId, int logicalDeviceId, long clientTypeId, List<SchedulableItem> profiles, List<SchedulableItem> registers, List<SchedulableItem> eventLogs, boolean readOldObisCodes) {
        this.originalId = originalId;
        this.scheduleId = scheduleId;
        this.logicalDeviceId = logicalDeviceId;
        this.clientTypeId = clientTypeId;
        this.profiles = profiles;
        this.registers = registers;
        this.eventLogs = eventLogs;
        this.readOldObisCodes = readOldObisCodes;
    }

    //JSon constructor
    private Beacon3100Schedulable() {
    }

    public Structure toStructure() {
        final Structure structure = initStructure();

        addItemsToStructure(structure, profiles);
        addItemsToStructure(structure, registers);
        addItemsToStructure(structure, eventLogs);

        return structure;
    }

    private Structure initStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getScheduleId()));
        structure.addDataType(new Unsigned16(getLogicalDeviceId()));
        structure.addDataType(new Unsigned32(getClientTypeId()));
        return structure;
    }

    private void addItemsToStructure(Structure structure, List<SchedulableItem> items) {
        if (items == null)
            return;
        final Array profileArray = new Array();
        for (SchedulableItem item : items) {
            profileArray.addDataType(OctetString.fromObisCode((ObisCode) item.getObisCode()));
        }
        structure.addDataType(profileArray);
    }

    public Structure toStructureForNewFirmware() {
        final Structure structure = initStructure();

        addItemsToStructureForNewFirmware(structure, profiles);
        addItemsToStructureForNewFirmware(structure, registers);
        addItemsToStructureForNewFirmware(structure, eventLogs);

        return structure;
    }

    @XmlAttribute
    public long getOriginalId() {
        return originalId;
    }

    private void addItemsToStructureForNewFirmware(Structure structure, List<SchedulableItem> items) {
        if (items == null)
            return;
        final Array profileArray = new Array();
        for (SchedulableItem item : items) {
            profileArray.addDataType(item.toStructure());
        }
        structure.addDataType(profileArray);
    }

    @XmlAttribute
    public long getScheduleId() {
        return scheduleId;
    }

    @XmlAttribute
    public int getLogicalDeviceId() {
        return logicalDeviceId;
    }

    @XmlAttribute
    public long getClientTypeId() {
        return clientTypeId;
    }

    @XmlAttribute
    public List<SchedulableItem> getProfiles() {
        return profiles;
    }

    @XmlAttribute
    public List<SchedulableItem> getRegisters() {
        return registers;
    }

    @XmlAttribute
    public List<SchedulableItem> getEventLogs() {
        return eventLogs;
    }

    public boolean updateBufferSizeForRegister(ObisCode obisCode, Unsigned16 bufferSize) {
        return updateBufferSize(obisCode, bufferSize, registers);
    }

    public void updateBufferSizeForAllRegisters(Unsigned16 bufferSize) {
        updateAllBufferSize(bufferSize, registers);
    }

    public boolean updateBufferSizeForLoadProfile(ObisCode obisCode, Unsigned32 bufferSize) {
        return updateBufferSize(obisCode, bufferSize, profiles);
    }

    public void updateBufferSizeForAllLoadProfiles(Unsigned32 bufferSize) {
        updateAllBufferSize(bufferSize, profiles);
    }

    public boolean updateBufferSizeForEventLogs(ObisCode obisCode, Unsigned32 bufferSize) {
        return updateBufferSize(obisCode, bufferSize, eventLogs);
    }

    public void updateBufferSizeForAllEventLogs(Unsigned32 bufferSize) {
        updateAllBufferSize(bufferSize, eventLogs);
    }

    private void updateAllBufferSize(AbstractDataType bufferSize, List<SchedulableItem> items) {
        for (SchedulableItem item : items) {
            item.setBufferSize(bufferSize);
        }
    }

    private boolean updateBufferSize(ObisCode obisCode, AbstractDataType bufferSize, List<SchedulableItem> items) {
        SchedulableItem item = SchedulableItem.findObisCode(obisCode, items);
        if (item != null) {
            item.setBufferSize(bufferSize);
            return true;
        }
        return false;
    }
}
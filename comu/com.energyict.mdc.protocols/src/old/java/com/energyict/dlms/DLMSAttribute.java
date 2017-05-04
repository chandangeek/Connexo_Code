package com.energyict.dlms;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.obis.ObisCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jme
 *
 */
public class DLMSAttribute{

	private final ObisCode	obisCode;
	private final int		attribute;
	private final int		classId;
    private final int       snAttribute;

    /**
     *
     * @param obisCode
     * @param attribute
     * @param classId
     */
    public DLMSAttribute(ObisCode obisCode, int attribute, int classId) {
        this.obisCode = obisCode;
        this.attribute = attribute;
        this.classId = classId;
        this.snAttribute = DLMSUtils.attrLN2SN(attribute);
    }

    /**
     *
     * @param obisCode
     * @param attribute
     */
    public DLMSAttribute(ObisCode obisCode, DLMSClassAttributes attribute) {
        this.obisCode = obisCode;
        this.attribute = attribute.getAttributeNumber();
        this.classId = attribute.getDlmsClassId().getClassId();
        this.snAttribute = attribute.getShortName();
    }

    /**
     *
     * @param obisCode
     * @param attribute
     * @param classId
     */
    public DLMSAttribute(ObisCode obisCode, int attribute, DLMSClassId classId) {
		this(obisCode, attribute, classId.getClassId());
	}

    /**
     *
     * @param dlmsAttribute
     * @return
     */
    public static DLMSAttribute fromString(String dlmsAttribute) {
        if (dlmsAttribute != null) {
            String[] strings = dlmsAttribute.split(":");
            if ((strings != null) && (strings.length == 3)) {
                try {
                    DLMSClassId classID = null;
                    if (strings[0].equals("?")) {
                        classID = DLMSClassId.UNKNOWN;
                    } else {
                        classID = DLMSClassId.findById(Integer.valueOf(strings[0]).intValue());
                    }
                    ObisCode obis = ObisCode.fromString(strings[1]);
                    int attribute = Integer.valueOf(strings[2]).intValue();
                    return new DLMSAttribute(obis, attribute, classID);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid dlmsAttribute string: " + dlmsAttribute);
                }
            }
        }
        throw new IllegalArgumentException("Invalid dlmsAttribute string: " + dlmsAttribute);
    }

    /**
     *
     * @param uo
     * @param attribute
     * @return
     */
    public static DLMSAttribute fromUniversalObject(UniversalObject uo, int attribute) {
        if (uo != null) {
            return new DLMSAttribute(uo.getObisCode(), attribute, uo.getDLMSClassId());
        }
        throw new IllegalArgumentException("UniversalObject 'uo' cannot be null.");
    }

    /**
     *
     * @param dlmsAttributes
     * @return
     */
    public static List<DLMSAttribute> getListOfAttributes(String... dlmsAttributes) {
        DLMSAttribute[] attributeArray = new DLMSAttribute[dlmsAttributes.length];
        for (int i = 0; i < dlmsAttributes.length; i++) {
            attributeArray[i] = fromString(dlmsAttributes[i]);
        }
        return getListOfAttibutes(attributeArray);
    }

    /**
     *
     * @param attributeArray
     * @return
     */
    private static List<DLMSAttribute> getListOfAttibutes(DLMSAttribute... attributeArray) {
        List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
        Collections.addAll(dlmsAttributes, attributeArray);
        return dlmsAttributes;
    }

	/**
	 * @param obisCodeAsString
	 * @param attribute
	 */
	public DLMSAttribute(String obisCodeAsString, int attribute, DLMSClassId classId) {
		this(ObisCode.fromString(obisCodeAsString), attribute, classId);
	}

	/**
	 * @return
	 */
	public ObisCode getObisCode() {
		return obisCode;
	}

	/**
	 * @return
	 */
	public int getAttribute() {
		return attribute;
	}

	/**
	 * @return
	 */
	public DLMSClassId getDLMSClassId() {
		return DLMSClassId.findById(getClassId());
	}

    /**
     * @return
     */
    public int getClassId() {
        return classId;
    }

    public int getSnAttribute() {
        return snAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DLMSAttribute)) {
            return false;
        }

        DLMSAttribute that = (DLMSAttribute) o;

        if (attribute != that.attribute) {
            return false;
        }
        if (classId != that.classId) {
            return false;
        }
        if (!obisCode.equals(that.obisCode)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = obisCode.hashCode();
        result = 31 * result + attribute;
        result = 31 * result + classId;
        return result;
    }

    @Override
    public String toString() {
        return "DLMSAttribute={obisCode=" + obisCode + ", attribute=" + attribute + ", classId=[" + classId + ", " + getDLMSClassId() + "]}";
    }

}

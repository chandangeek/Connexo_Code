package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

public enum RenewGMKSingleActionScheduleAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0x01),
    EXECUTED_SCRIPT(2, 0x02),
    TYPE(3, 0x03),

    /**
     * Specifies the time and the date when the script is executed.
     * array of execution_time_date
     * execution_time_date ::= structure {
     *   time: octet-string,
     *   date: octet-string
     * }
     * The two octet-strings contain time and date, in fixed order, formatted as specified in 4.1.6.1
     * of the DLMS blue book [29]. Hundredths of seconds shall be zero. Fields left as ‘unspecified’
     * are interpreted as wildcards. These can be used for specifying recurring actions. Setting this
     * field to date and time undefined (all fields set to 0xff) cancels the scheduled execution date.
     */
    EXECUTION_TIME(4, 0x04);

    private final int attributeNumber;
    private final int shortName;

    RenewGMKSingleActionScheduleAttributes(int attrNr, int sn) {
        this.attributeNumber = attrNr;
        this.shortName = sn;
    }

    /**
     * Getter for the attribute number
     *
     * @return the attribute number as int
     */
    public int getAttributeNumber() {
        return this.attributeNumber;
    }

    public DLMSClassId getDlmsClassId() {
        return DLMSClassId.SINGLE_ACTION_SCHEDULE;
    }

    /**
     * Getter for the short name
     *
     * @return the short name as int
     */
    public int getShortName() {
        return this.shortName;
    }

    public static RenewGMKSingleActionScheduleAttributes findByAttributeNumber(int attribute) {
        for (RenewGMKSingleActionScheduleAttributes limiterAttribute : RenewGMKSingleActionScheduleAttributes.values()) {
            if (limiterAttribute.getAttributeNumber() == attribute) {
                return limiterAttribute;
            }
        }
        throw new IllegalArgumentException("No attributeNumber found for id = " + attribute);
    }

    public DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
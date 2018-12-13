package com.elster.protocolimpl.lis100.registers;

import com.energyict.obis.ObisCode;

/**
 * User: heuckeg
 * Date: 08.08.11
 * Time: 10:49
 */
@SuppressWarnings({"unused"})
public class Lis100Register {

    public static final int STATUS_REGISTER = 1;
    public static final int SOFTWARE_VERSION = 2;
    public static final int SENSOR_NUMBER = 3;
    public static final int H1 = 4;
    public static final int H2 = 5;
    public static final int H2BOM = 6;

    /**
     * The obisCode from the register
     */
    private final ObisCode obisCode;
    /**
     * channel where the value resides
     */
    private final int channel;
    /**
     * identifier for value
     */
    private final int ident;
    /**
     * name of code
     */
    private final String desc;

    /**
     * constructor for Lis100Register object
     *
     * @param obisCode - EIS code
     * @param channel  - channel where value is stored
     * @param ident    - identifying number for value
     * @param desc     - description of value
     */
    public Lis100Register(ObisCode obisCode, int channel, int ident, String desc) {
        this.obisCode = obisCode;
        this.channel = channel;
        this.ident = ident;
        this.desc = desc;
    }

    /**
     * getter for ObisCode of Lis100Register object
     *
     * @return EI server obis code
     */
    public ObisCode getObisCode() {
        return this.obisCode;
    }

    /**
     * getter for channel where value is stored
     *
     * @return channel no
     */
    public int getChannel() {
        return channel;
    }

    /**
     * getter for identifying number of value
     *
     * @return ident no
     */
    public int getIdent() {
        return ident;
    }

    /**
     * getter for description of Lis100Register object
     *
     * @return descriptive string
     */
    public String getDesc() {
        return desc;
    }
}

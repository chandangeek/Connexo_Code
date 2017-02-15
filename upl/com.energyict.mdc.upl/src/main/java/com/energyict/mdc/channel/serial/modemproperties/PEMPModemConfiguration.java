package com.energyict.mdc.channel.serial.modemproperties;


/**
 * @author Koen
 * @since 19 april 2004, 10:14
 */
public enum PEMPModemConfiguration {

    SHC("SHC", "FENS", "COM", new String[]{"234291904506", "234291848117", "234291613074", "234291330112"}),
    NHC("NHC", "FENS", "COM", new String[]{"234291904506", "234291848117", "234291613074", "234291330112"}),
    LN("LN", "FENS", "COM", new String[]{"234291904506", "234291848117", "234291613074", "234291330112"}),
    SC("SC", "FENS", "COM", new String[]{"234291904506", "234291848117", "234291613074", "234291330112"}),
    MIN("MIN", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    MIS("MIS", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    WWN("WWN", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    WWS("WWS", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    NE("NE", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    NW("NW", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    NI("NI", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    WW("WW", "FENS", "COM", new String[]{"234291613074", "234291330112", "234291904506", "234291848117"}),
    MID("MID", "FENS", "COM", new String[]{"234291904506", "234291848117", "234291613074", "234291330112"});

    private String key;
    private String promptResponse;
    private String connectionResponse;
    private String[] addresses;

    PEMPModemConfiguration(String key, String promptResponse, String connectionResponse, String[] addresses) {
        this.key = key;
        this.promptResponse = promptResponse;
        this.connectionResponse = connectionResponse;
        this.addresses = addresses;
    }

    /**
     * Get the corresponding PEMPModemConfiguration for the given key.
     *
     * @param key The key to search for
     * @return the corresponding PEMPLookup
     * @throws IllegalArgumentException when no corresponding PEMPModemConfiguration could be found
     */
    public static PEMPModemConfiguration getPEMPModemConfiguration(String key) {
        for (PEMPModemConfiguration modemConfiguration : values()) {
            if (modemConfiguration.getKey().equals(key)) {
                return modemConfiguration;
            }
        }
        throw new IllegalArgumentException("Unknown PEMP modem configuration: '" + key + "'");
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append(key + " " + promptResponse + " " + promptResponse + " " + connectionResponse + " " + connectionResponse);
        for (int i = 0; i < getAddresses().length; i++) {
            strBuff.append(" " + getAddresses()[i]);
        }
        return strBuff.toString();
    }

    /**
     * Getter for property key.
     *
     * @return Value of property key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Getter for property promptResponse.
     *
     * @return Value of property promptResponse.
     */
    public String getPromptResponse() {
        return promptResponse;
    }

    /**
     * Getter for property connectionResponse.
     *
     * @return Value of property connectionResponse.
     */
    public String getConnectionResponse() {
        return connectionResponse;
    }

    /**
     * Getter for property addresses.
     *
     * @return Value of property addresses.
     */
    public String[] getAddresses() {
        return this.addresses;
    }
}

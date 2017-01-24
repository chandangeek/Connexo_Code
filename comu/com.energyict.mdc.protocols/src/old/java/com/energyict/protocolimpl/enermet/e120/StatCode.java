package com.energyict.protocolimpl.enermet.e120;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Protocol indicates the status of a response with a "StatCode" integer.
 *
 * @author fbo
 */
class StatCode {

    private static Map all = new LinkedHashMap();

    /** OK */
    final public static StatCode OK =
        create(0x00, "OK");

    /** Version of received frame is not supported */
    final public static StatCode VERSION_NOT_SUPPORTED =
        create(0x01, "Version not supported");

    /** ID number of received message is unknown */
    final public static StatCode MESSAGE_NOT_SUPPORTED =
        create(0x02, "Message not supported");

    /** Target address is illegal */
    final public static StatCode ILLEGAL_ADDRESS =
        create(0x0A, "Illegal address");

    /** If device does not return first byte of response in certain time, empty
     * response with this status is returned */
    final public static StatCode TIME_OUT_READING_DEVICE =
        create(0x81, "Time out readign device, first byte");

    /** If device does not return some bytes of response beyond the first one in
     * certain time, empty response with this status is returned.  This error
     * is thrown also when frame is incorrect. */
    final public static StatCode MEMORY_READ_ERROR =
        create(0x82, "Memory read error");

    /** If reading flash raises any exception during command completion, emtpy
     * response with this status is returned. */
    final public static StatCode MEMORY_WRITE_ERROR =
        create(0x83, "Memory write error");

    /** If using AT commands of the module raises any exception during command
     * completion, emtpty response with this status is returned */
    final public static StatCode AT_COMMAND_EXCEPTION =
        create(0x84, "AT command exception");

    /** If module does not identify format of any message that is addressed to
     * it, empty response with this status is returned.  This applies also to
     * series read messages. */
    final public static StatCode UNKNOWN_MESSAGE_FORMAT =
        create(0x85, "Unknown message format");

    /** If received configuration message was bigger version number than module
     * software supportes, this status is returned. */
    final public static StatCode UNKNOWN_CONFIGURATION_VERSION =
        create(0x86, "Unknown configuration version");

    /** If received configuration message has "wrong" password, this status is
     * returned. */
    final public static StatCode WRONG_CONFIGURATION_PASSWORD =
        create(0x87, "Wrong configuration password");

    /** When communication module receives invalid application message from ODEP
     * interface, this status is returned. */
    final public static StatCode INVALID_ODEP_MSG =
        create(0x88, "Invalid ODEP message");

    /** When communication module receives invalid application message from IDEP
     * interface, this status is returned. */
    final public static StatCode INVALID_IDEP_MSG =
        create(0x89, "Invalid IDEP message");

    /** When communication module receives invalid IDEP frame this status is
     * returned. */
    final public static StatCode INVALID_IDEP_FRAME =
        create(0x8A, "Invalid IDEP frame");

    /** When communication module receives IDEP frame with illegal checksum,
     * this status is returned. */
    final public static StatCode ILLEGAL_IDEP_CHECKSUM =
        create(0x8B, "Illegal IDEP checksum");

    /** When communication module receives IDEP frame with wrong sequence
     * number, this status is returned. */
    final public static StatCode WRONG_IDEP_SEQUENCE =
        create(0x8C, "Wrong IDEP sequence");

    /** When communication module receives too short applicatin message from
     * ODEP interface, this status is returned */
    final public static StatCode TOO_SHORT_ODEP_MSG =
        create(0x8D, "Too short ODEP message");

    /** When software detects an implementation error in won code, this status
     * is returned */
    final public static StatCode SOFTWARE_ERROR =
        create(0x8E, "Software error");

    /** When communication module tries to deliver too long IDEP message to
     * meter, this status is returned. */
    final public static StatCode TOO_LONG_FOR_IDEP =
        create(0x90, "Too long for IDEP");

    /** Jave runtime exception has been catch.  Many reasons may cause this:
     * Out of memory, Empty stack, Null pointer, Invalid array index ...*/
    final public static StatCode RUN_TIME_EXCEPTION =
        create(0x91, "Run time exception");

    /** If received configuration is illegal, then this status is returned */
    final public static StatCode INVALID_CONFIGURATION =
        create(0x92, "Invalid configuration");

    /** When communication module tries to deliver too long ODEP message to AMR,
     * this status is returned. */
    final public static StatCode TOO_LONG_FOR_ODEP =
        create(0x93, "Too long for ODEP");

    /** If an exception that is not specially defined in this table is raised
     * during command completion, empty response with this status is returned.*/
    final public static StatCode UNDEFINED_EXCEPTION =
        create(0xF0, "Undefined exception");

    private int id;
    private String name;

    static StatCode get(int id){
        return (StatCode)all.get(new Integer(id));
    }

    private StatCode( int id, String name) {
        this.id = id;
        this.name = name;
    }

    private static StatCode create(int id, String name){
        StatCode sc = new StatCode(id, name);
        all.put(new Integer(id), sc);
        return sc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return "StatCode [" + id + ", " + name + "]";
    }

}

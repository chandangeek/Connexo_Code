/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author fbo
 */
class NackCode {

    private static Map all = new LinkedHashMap();

    /** OK */
    final public static NackCode OK =
        create(0x00, "OK");

    /** Length of message is wrong */
    final public static NackCode LENGTH_ERROR =
        create(0x01, "Length Error");

    /** Value of parameter in message is wrong */
    final public static NackCode PARAMETER_ERROR =
        create(0x02, "Parameter error");

    /** Execution of messages at this authentication level is not possible */
    final public static NackCode AUTHENTICATION_LEVEL_ERROR =
        create(0x03, "Authentication level error");

    /** Execution of message at this operation mode of device is not possible */
    final public static NackCode OPERATION_MODE_ERROR =
        create(0x04, "Operation mode error");

    /** Execution of message is not possible e.g.time adjust too big */
    final public static NackCode EXECUTION_ERROR =
        create(0x05, "Execution error");

    /** Reading or writing error fo (non-volatile) memory */
    final public static NackCode MEMORY_ACCESS_ERROR =
        create(0x06, "Memory access error");

    /** Name or password of authentication message does not mach */
    final public static NackCode AUTHENTICATION_ERROR =
        create(0x14, "Authentication error");

    /** AT-command returns failure status */
    final public static NackCode AT_COMMAND_ERROR =
        create(0x15, "AT command error");

    /** AT command execution error */
    final public static NackCode CAN_NOT_EXECUTE_AT_COMMAND =
        create(0x16, "AT command execution error");

    /** Can not write to a file */
    final public static NackCode FILE_WRITING_ERROR =
        create(0x17, "File writing error");

    /** Given configuration was illegal */
    final public static NackCode ILLEGAL_CONFIGURATION =
        create(0x18, "Illegal configuration");

    private int id;
    private String name;

    static NackCode get(int id){
        return (NackCode)all.get(new Integer(id));
    }

    private NackCode( int id, String name) {
        this.id = id;
        this.name = name;
    }

    private static NackCode create(int id, String name){
        NackCode sc = new NackCode(id, name);
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

    public boolean isOk(){
        return OK.equals(this);
    }

    public String toString(){
        return "NackCode [ 0x" + Integer.toHexString(id) + ", " + name + "]";
    }

}

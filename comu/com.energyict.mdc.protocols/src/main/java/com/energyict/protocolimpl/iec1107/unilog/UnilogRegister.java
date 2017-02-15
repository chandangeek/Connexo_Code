/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.unilog;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;

public class UnilogRegister {

    private final String name;
    private final ObisCode obis;
    private final String registerId;
    private final Unit unit;
    private final int type;
    private final boolean cached;
    private final boolean writable;

    /**
     * @param name
     * @param obis
     * @param registerId
     * @param unit
     */
    public UnilogRegister(String name, String obis, String registerId, Unit unit, int type, boolean cached, boolean writable) {
        this.name = name == null ? obis.toString() : name;
        this.obis = ObisCode.fromString(obis);
        this.registerId = registerId;
        this.unit = unit;
        this.type = type != -1 ? type : VDEWRegister.VDEW_QUANTITY;
        this.cached = cached;
        this.writable = writable;
    }

    /**
     * @param name
     * @param obis
     * @param registerId
     * @param unit
     */
    public UnilogRegister(String name, String obis, String registerId, Unit unit, int type, boolean cached) {
        this(name, obis, registerId, unit, type, cached, false);
    }

    /**
     * @param name
     * @param obis
     * @param registerId
     * @param unit
     */
    public UnilogRegister(String name, String obis, String registerId, Unit unit, int type) {
        this(name, obis, registerId, unit, type, false);
    }

    /**
     * @param name
     * @param obis
     * @param registerId
     * @param unit
     */
    public UnilogRegister(String name, String obis, String registerId, Unit unit) {
        this(name, obis, registerId, unit, -1);
    }

    /**
     * @param name
     * @param obis
     * @param registerId
     */
    public UnilogRegister(String name, String obis, String registerId) {
        this(name, obis, registerId, null);
    }

    /**
     * @param obis
     * @param registerId
     */
    public UnilogRegister(String obis, String registerId) {
        this(null, obis, registerId);
    }

    /**
     * @return
     */
    public VDEWRegister getVdewRegister() {
        return new VDEWRegister(getRegisterId(), getType(), 0, -1, getUnit(), isWritable(), isCached());
    }

    public String getName() {
        return name;
    }

    public ObisCode getObis() {
        return obis;
    }

    public String getRegisterId() {
        return registerId;
    }

    public Unit getUnit() {
        return unit;
    }

    public int getType() {
        return type;
    }

    public boolean isCached() {
        return cached;
    }

    public boolean isWritable() {
        return writable;
    }
}

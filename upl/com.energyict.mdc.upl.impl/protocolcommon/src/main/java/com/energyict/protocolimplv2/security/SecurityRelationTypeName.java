package com.energyict.protocolimplv2.security;

/**
 * Summarizes all the usable SecurityRelationType names.
 * Each different set of security properties requires a relationTable to be created.
 * This overview should eliminate overlap or duplications.
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:54
 */
public enum SecurityRelationTypeName {

    DLMSSECURITY("DlmsSecurity");

    private final String name;

    private SecurityRelationTypeName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

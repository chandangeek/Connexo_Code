package com.elster.protocolimpl.lis200.utils;

/**
 * User: heuckeg
 * Date: 28.10.2010
 * Time: 13:43:04
 */
public class ArchiveColumn {

    public static boolean isGONO(String name) {
        return name.equalsIgnoreCase("GONR") || name.equalsIgnoreCase("GONO");
    }

    public static boolean isONO(String name) {
        return name.equalsIgnoreCase("AONR") || name.equalsIgnoreCase("AONO");
    }

    public static boolean isTST(String name) {
        return name.equalsIgnoreCase("ZEIT") || name.equalsIgnoreCase("TIME") || name.equalsIgnoreCase("TST");
    }

    public static boolean isSystemState(String name) {
        return name.equalsIgnoreCase("ST.SY") || name.equalsIgnoreCase("STSY");
    }

    public static boolean isInstState(String name) {
        try {
            return name.toUpperCase().startsWith("ST.") &&
                    (Integer.parseInt(name.substring(3)) > 0);
        }
        catch (Exception e) {
            return false;
        }
    }

    public static boolean isEvent(String name) {
        return name.equalsIgnoreCase("S.AEN") ||
                name.equalsIgnoreCase("ER") ||
                name.equalsIgnoreCase("EV") ||
                name.equalsIgnoreCase("EVTR") ||
                name.equalsIgnoreCase("EVNT") ||
                name.equalsIgnoreCase("STAE");
    }
}

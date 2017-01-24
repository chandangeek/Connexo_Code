package com.energyict.protocolimplv2.elster.ctr.MTU155.info;

/**
 * Copyrights EnergyICT
 * Date: 4-nov-2010
 * Time: 11:12:57
 */
public class EquipmentClassInfo {

    private static final String[][] CLASS_INFO = new String[][]{
            new String[]{"000", "Equipment of non defined class"},
            new String[]{"AAA", "Class A equipment"},
            new String[]{"BBB", "Class B equipment"},
            new String[]{"CCC", "Class C equipment"},
            new String[]{"BVO", "Equipment of class B TYPE 1 (Volumetric)"},
            new String[]{"CVO", "Equipment of class C TYPE 2 (Volumetric)"},
            new String[]{"CVE", "Equipment of class C TYPE 2 (Venturi meter)"},
            new String[]{"CEM", "Equipment of class C Energy Meter"},
            new String[]{"AA1", "Equipment of type A1"},
            new String[]{"AA2", "Equipment of type A2"},
    };

    /**
     * Gives a matching description per equipment class
     * @param eqClass: equipment class code
     * @return a matching description
     */
    public static String getEquipmentClass(String eqClass) {
        for (String[] classInfo : CLASS_INFO) {
            if ((classInfo.length >= 2) && (eqClass.trim().equals(classInfo[0]))) {
                return "[" + eqClass + "] " + classInfo[1];
            }
        }

        if (eqClass.startsWith("R")) {
            return "[" + eqClass + "] " + "Repeater of type " + eqClass.substring(1);
        } else if (eqClass.startsWith("T")) {
            return "[" + eqClass + "] " + "Translator of type " + eqClass.substring(1);
        }

        return "[" + eqClass + "] " + "Other";

    }


}

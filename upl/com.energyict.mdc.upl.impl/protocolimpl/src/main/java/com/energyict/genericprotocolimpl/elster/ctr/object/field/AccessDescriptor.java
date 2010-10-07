package com.energyict.genericprotocolimpl.elster.ctr.object.field;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:16:41
 */
public class AccessDescriptor {
    private int access;
    public final int LENGTH = 1;
    public AccessDescriptor(int access) {
        this.access = access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public int getAccess() {
        return access;
    }

    public String getWritePermissions() {
        String permissions = "";
        for (int i = 0; i < 4; i++) {
            if ((((access & 0x0F0) >> (7 - i)) & 0x01) == 1) {
                switch (i) {
                    case 0:
                        permissions += "A";
                        break;
                    case 1:
                        permissions += "B";
                        break;
                    case 2:
                        permissions += "C";
                        break;
                    case 3:
                        permissions += "D";
                        break;
                }
            }
        }
        return permissions;
    }


    public String getReadPermissions() {
        String permissions = "";
        for (int i = 0; i < 4; i++) {
            if ((((access & 0x0F) >> (3 - i)) & 0x01) == 1) {
                switch (i) {
                    case 0:
                        permissions += "A";
                        break;
                    case 1:
                        permissions += "B";
                        break;
                    case 2:
                        permissions += "C";
                        break;
                    case 3:
                        permissions += "D";
                        break;
                }
            }
        }
        return permissions;
    }
}
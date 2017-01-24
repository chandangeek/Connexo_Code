package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.Field;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

/**
 * Class for the access field of a CTR Object. Indicates the enabled permissions.
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:16:41
 */
public class AccessDescriptor extends AbstractField {

    private int access;

    public AccessDescriptor(int access) {
        this.access = access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public int getAccess() {
        return access;
    }

    /**
     * Checks the write permissions for a certain access code
     * @return the write permissions
     */
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

    /**
     * Checks the read permissions for a certain code
     * @return the read permissions
     */
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

    public byte[] getBytes() {
        return new byte[]{(byte) getAccess()};
    }

    public Field parse(byte[] rawData, int offset) throws CTRParsingException {
        setAccess(rawData[offset]);
        return this;
    }

    public int getLength() {
        return 1;
    }
}
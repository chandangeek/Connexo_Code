package com.energyict.protocolimplv2.messages.enums;

/**
 * Copyrights EnergyICT
 * Date: 12/10/16
 * Time: 13:59
 */
public enum SetDisplayMode {

        SET_DIPLAY_ON(1, "Set Display ON"),
        SET_DIPLAY_OFF(2, "Set Display OFF");

        private final int attributeNumber;
        private final String modeName;

        /**
         * Default constructor
         *
         * @param attributeNumber the number of the attribute (1-based)
         * @param modeName description of the attribute
         */
        private SetDisplayMode(int attributeNumber, String modeName) {
            this.attributeNumber = attributeNumber;
            this.modeName = modeName;
        }

        public static int fromModeName(String description) {
            for (com.energyict.protocolimplv2.messages.enums.SetDisplayMode modes : values()) {
                if (modes.getModeName().equals(description)) {
                    return modes.getAttributeNumber();
                }
            }
            return 0;
        }

        public static String[] getModeNames() {
            String[] result = new String[values().length];
            for (int index = 0; index < values().length; index++) {
                result[index] = values()[index].getModeName();
            }
            return result;
        }

        /**
         * Getter for the attribute number
         *
         * @return the attribute number as int
         */
        public int getAttributeNumber() {
            return this.attributeNumber;
        }

        /**
         * Getter for the short name
         *
         * @return the short name as int
         */
        public String getModeName() {
            return this.modeName;
        }

    }


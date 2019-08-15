package com.energyict.protocolimplv2.messages.enums;

public class DaysOfMonth{
    public static final int DLMS_NOT_SPECIFIED = 0xFF;

    public enum DLMSEncodings{
        SECOND_LAST_DAY_OF_THE_MONTH(0xFD, "Second to last day of the month"),
        LAST_DAY_OF_THE_MONTH (0xFE, "Last day of the month"),
        ALL_DAYS (0xFF, "All days");

        private final int dlmsCode;
        private final String description;

        DLMSEncodings(int dlmsCode, String description) {
            this.dlmsCode = dlmsCode;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getDlmsCode(){
            return dlmsCode;
        }

        protected static int getDlmsCode(String description){
            for (final DLMSEncodings item : DLMSEncodings.values()) {
                if (item.getDescription().equals(description)) {
                    return item.getDlmsCode();
                }
            }

            return DLMS_NOT_SPECIFIED;
        }
    }


    public static final int DAYS = 31;

    public static String[] getDaysOfMonthValues(){
        String[] daysOfMonth = new String[DAYS + 3];
        for (int i=0; i<31; i++){
            daysOfMonth[i] = String.format("%02d", i+1);
        }
        int k = 0;
        for (DLMSEncodings item : DLMSEncodings.values()){
            daysOfMonth[DAYS + k++] = item.getDescription();
        }

        return daysOfMonth;
    }

    public static int getDlmsEncoding(String selectedDayOfMonth) {
        if (selectedDayOfMonth.matches("\\d+")) { // check if's numbers-only
            int day = Integer.parseInt(selectedDayOfMonth);
            if (day >= 1 && day <= 31) { //extra-check, just in case
                return day;
            } else {
                return DLMS_NOT_SPECIFIED;
            }
        } else {
            // if it's not a number try to get DLMS codes
            return DLMSEncodings.getDlmsCode(selectedDayOfMonth);
        }
    }
}
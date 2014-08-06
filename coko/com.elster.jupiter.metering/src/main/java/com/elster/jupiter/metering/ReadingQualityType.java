package com.elster.jupiter.metering;

public final class ReadingQualityType {

    public static final String MDM_VALIDATED_OK_CODE = "3.0.1";

    private final String code;

    public ReadingQualityType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReadingQualityType defaultCodeForRuleId(long id) {
        return new ReadingQualityType("3.6." + id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return code.equals(((ReadingQualityType) o).code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}

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

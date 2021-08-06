package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.util.LittleEndianData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UmiCode extends LittleEndianData {
    public static final int SIZE = 4;
    private static final String UINT_8_PATTERN = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    public static final Pattern UMI_CODE_PATTERN = Pattern.compile("umi\\." +
            UINT_8_PATTERN + "\\." +
            UINT_8_PATTERN + "\\." +
            UINT_8_PATTERN + "\\." +
            UINT_8_PATTERN);

    private final String code;
    
    public UmiCode(String code) {
        super(SIZE);
        Matcher matcher = UMI_CODE_PATTERN.matcher(code);
        if (matcher.matches()) {
            getRawBuffer().put((byte)Short.parseShort(matcher.group(4)))
                    .put((byte)Short.parseShort(matcher.group(3)))
                    .put((byte)Short.parseShort(matcher.group(2)))
                    .put((byte)Short.parseShort(matcher.group(1)));
            this.code = code;
        } else {
            throw new java.security.InvalidParameterException(
                    "Invalid umi code=" + code + ", required format=\"umi.byte3.byte2.byte1.byte0\"");
        }
    }

    public UmiCode(byte[] rawCode) {
        super(rawCode, SIZE, false);
        code = "umi." + Byte.toUnsignedInt(rawCode[3]) + "." +
                Byte.toUnsignedInt(rawCode[2]) + "." +
                Byte.toUnsignedInt(rawCode[1]) + "." +
                Byte.toUnsignedInt(rawCode[0]);
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static UmiCode fromString(String code) {
        return new UmiCode(code);
    }
}

package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;

import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TelegramDataVariableField extends TelegramDataField{

    private List<String> payload;
    private int lVar;

    public void addVariableLengthPayload(List<String> payload) {
        this.payload = payload;
    }

    @Override
    public void parse() {
        parse(true);
    }

    /**
     * With data field = ‘1101b´ several data types with variable length can be used. The length of the data is given
     * after the DRH with the first byte of real data, which is here called LVAR (e.g. LVAR = 02h: ASCII string with
     * two characters follows).
     */
    @Override
    public void parse(boolean isProfile) {
        int lVar = getLvar();
        int length = 0;

        this.parsedValue = "";

        if (isProfile) {
            length = lVar;
            parseVariableLengthFieldAsISO8859();
        } else {

        /*  LVAR = 00h - BFh        8-bit text string according to ISO 8859-1 with LVAR (0 to 191) characters.
                                    Note that a text string (like all other mutibyte data) is transmitted "Least significant byte first" */
            if ((lVar >= 0x00) && (lVar <= 0xBF)) {
                length = lVar;
                //System.out.println("LVAR = " + lVar + ": text string, length=" + length);
                parseVariableLengthFieldAsISO8859();
            }
            /* LVAR = C0h - C9h         positive BCD number with (LVAR - C0h, i.e. 0 to 9) • 2 digits (0 to 18 digits) */
            else if ((lVar >= 0xC0) && (lVar <= 0xC9)) {
                length = lVar - 0xC0;
                //System.out.println("LVAR = " + lVar + ": positive BCD, length=" + length);
            }
            /* LVAR = D0h - D9h         negative BCD number with (LVAR - D0h) • 2 digits (0 to 18 digits) */
            else if ((lVar >= 0xD0) && (lVar <= 0xD9)) {
                length = lVar - 0xD0;
                //System.out.println("LVAR = " + lVar + ": negative BCD, length=" + length);
            }
            /* LVAR = E0h - Efh        Binary number with (LVAR - E0h) bytes (0 to 15 bytes) */
            else if ((lVar >= 0xE0) && (lVar <= 0xEF)) {
                length = lVar - 0xE0;
                //System.out.println("LVAR = " + lVar + ": Binary number, length=" + length);
            }
            /* LVAR = F8h              floating point number according to IEEE 754 */
            else if (lVar == 0xF8) {
                length = lVar - 0xE0;
                //System.out.println("LVAR = " + lVar + ": floating point number according to IEEE 754, length=" + length);
            } else {
                //System.err.println("Don't know how to interpret LVAR=" + String.format("%02x", lVar));
            }
        }
    }


    /**
     * With data field = ‘1101b´ several data types with variable length can be used. The length of the data is given
     * after the DRH with the first byte of real data, which is here called LVAR (e.g. LVAR = 02h: ASCII string with
     * two characters follows).
     * @return
     */
    public int getVariableLengthFieldLength(String hexlVar) {
        this.lVar = Converter.hexToInt(hexlVar);

        int length = -1;

        /*  LVAR = 00h - BFh        8-bit text string according to ISO 8859-1 with LVAR (0 to 191) characters.
                                    Note that a text string (like all other mutibyte data) is transmitted "Least significant byte first" */
        if ((lVar >= 0x00) && (lVar <= 0xBF)) {
            length = lVar;
        }
        /* LVAR = C0h - C9h         positive BCD number with (LVAR - C0h, i.e. 0 to 9) • 2 digits (0 to 18 digits) */
        else if ((lVar >= 0xC0) && (lVar <= 0xC9)) {
            length = lVar - 0xC0;
        }
        /* LVAR = D0h - D9h         negative BCD number with (LVAR - D0h) • 2 digits (0 to 18 digits) */
        else if ((lVar >= 0xD0) && (lVar <= 0xD9)) {
            length = lVar - 0xD0;
        }
        /* LVAR = E0h - Efh        Binary number with (LVAR - E0h) bytes (0 to 15 bytes) */
        else if ((lVar >= 0xE0) && (lVar <= 0xEF)) {
            length = lVar - 0xE0;
        }
        /* LVAR = F8h              floating point number according to IEEE 754 */
        else if (lVar == 0xF8) {
            length = lVar - 0xE0;
        }
        else {
            System.err.println("Don't know how to interpret LVAR=" + String.format("%02x", lVar));
        }
        return length;
    }

    private int getLvar() {
        return lVar;
    }

    private int getSpacingControl() {
        return Converter.hexToInt(this.fieldParts.get(1));
    }

    private int getSpacingValue() {
        return Converter.hexToInt(this.fieldParts.get(2));
    }

    /*
        WEEKLY_LOG_DIF                   =   0x8D,
        WEEKLY_LOG_DIFE                  =   0x04,
        WEEKLY_LOG_VIF                   =   0x93,
        WEEKLY_LOG_VIFE                  =   0x1F,
        WEEKLY_LOG_LVAR                  =   0x2C,
        WEEKLY_LOG_SPACING_CONTROL_BYTE  =   0xF3,
        WEEKLY_LOG_SPACING_VALUE         =   0x01,
    */
    private void parseVariableLengthFieldAsISO8859() {
        int spacingControl = getSpacingControl();
        int spacingValue = getSpacingValue();

        StringBuilder parsedValues = new StringBuilder();

        this.parsedIntervals = new HashMap<>();

        parsedValues.append("--- profile ---").append("\n");
        parsedValues.append("\t * spacing control: ").append(spacingControl).append(" = ").append(String.format("%02x", spacingControl)).append("\n");
        parsedValues.append("\t * spacing value: ").append(spacingValue).append(" = ").append(String.format("%02x", spacingValue)).append("\n");

        int size = 3; // weekly
        if (spacingControl == 0xe2 || spacingControl == 0xd2) { // hourly and nightline - TODO check what's the rule here
            size = 2;
        }

        int start = 0;
        int id = 0;

        while (start + size < payload.size()){
            List<String> part = payload.subList(start, start + size);
            Collections.reverse(part);

            int value;

            if (size == 3 ) {
                int b1 = Integer.parseInt(part.get(0), 16);
                int b2 = Integer.parseInt(part.get(1), 16);
                int b3 = Integer.parseInt(part.get(2), 16);

                // force to 4 bytes to let Java handle the sign
                value = (b1 * 0x10000 + b2 * 0x100 + b3) * 0x100;
                // back to the real value, but now it's signed
                value = value / 0x100;
            } else {
                int b1 = Integer.parseInt(part.get(0), 16);
                int b2 = Integer.parseInt(part.get(1), 16);

                // force to 4 bytes to let Java handle the sign
                value = (b1 * 0x100 + b2) * 0x10000;
                // back to the real value, but now it's signed
                value = value / 0x10000;
            }

            this.parsedIntervals.put(id, (long) value);
            String parsedValue = "\t#" + id + "> " + value;

            parsedValues.append(parsedValue).append("\n");

            id++;
            start += size;
        }

        this.parsedValue = parsedValues.toString();
    }

}

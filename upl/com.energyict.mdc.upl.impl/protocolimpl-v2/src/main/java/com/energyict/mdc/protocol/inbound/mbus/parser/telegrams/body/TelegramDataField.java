package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.body;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Converter;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.DateCalculator;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.Measure_Unit;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.util.TelegramEncoding;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramDataField extends TelegramField {

    private TelegramVariableDataRecord parent;
    private static int REAL_FRACTION = 0x7FFFFF;		// 23-Bit (fraction of real)
    private static int REAL_EXPONENT = 0x7F80000;		// 24-Bit to 31-Bit (exponent of real)
    private static int REAL_SIGN = 0x80000000;			// 32-Bit (signum of real)
    private static int SIGN = 0x01;						// mask for signum

    public TelegramDataField() {
        super();
    }

    public TelegramDataField(TelegramVariableDataRecord parent) {
        this.parent = parent;
    }


    public void parse() {
        TelegramEncoding enc = this.parent.getDif().getDataFieldEncoding();

        int length = this.parent.getDif().getDataFieldLength();
        Measure_Unit unit = this.parent.getVif().getmUnit();
        int multiplier = this.parent.getVif().getMultiplier();

        if (length != this.fieldParts.size()) {
            System.out.println("ERROR: wrong size");
            // TODO: throw exception
            return;
        }

        if (this.parseDate(unit)) {
            // value is parsed, we are done here
            return;
        }

        List<String> fieldsRev = new ArrayList<>(this.fieldParts);
        Collections.reverse(fieldsRev);

        switch (enc) {
            case ENCODING_INTEGER:
                this.parseInteger(Converter.hexToBigInteger(Converter.convertListToString(fieldsRev)));
                break;
            case ENCODING_BCD:
                this.parseBCDAsBigInteger(Converter.hexToBigInteger(Converter.convertListToString(fieldsRev)));
                break;
            case ENCODING_REAL:

                break;
            case ENCODING_VARIABLE_LENGTH:
                System.err.println("Dont know how to parse ENCODING_VARIABLE_LENGTH " + Converter.convertListToString(fieldsRev));
                // TODO: parse variable LENGTH
                break;
            case ENCODING_NULL:
                System.err.println("Dont know how to parse ENCODING_NULL " + Converter.convertListToString(fieldsRev));
                // TODO: exception
                break;
            case ENCODING_USER_DEFINED_CELL_ID:
                // each bit has a different meaning;
                break;
            default:
                System.err.println("Dont know how to parse field of encoding: " + enc + ", value: " + Converter.convertListToString(fieldsRev));
                // TODO: exception
                break;
        }
    }



    /*
     * returns true if the value of this field is a date-type. Values
     * are parsed as dates (if it is a valid date-type) in this function as well.
     */
    public boolean parseDate(Measure_Unit dateType) {
        switch (dateType) {
            case DATE:
                // Type G: Day.Month.Year
                this.parsedValue = DateCalculator.getDate(this.fieldParts.get(0), this.fieldParts.get(1), false);

                break;
            case DATE_TIME:
                // Type F: Day.Month.Year Hour:Minute
                this.parsedValue = DateCalculator.getDateTime(this.fieldParts.get(0), this.fieldParts.get(1), this.fieldParts.get(2), this.fieldParts.get(3), false);
                break;
            case TIME:
                // Typ J: Hour:Minute:Second
                this.parsedValue = DateCalculator.getTimeWithSeconds(this.fieldParts.get(0), this.fieldParts.get(1), this.fieldParts.get(2));
                break;
            case DATE_TIME_S:
                // Typ I: Day.Month.Year Hour:Minute:Second
                /*
                Byte/
bit
msb lsb
lsB 8 7 6 5 4 3 2 1
16 15 14 13 12 11 10 9
24 23 22 21 20 19 18 17
32 31 30 29 28 27 26 25
40 39 38 37 36 35 34 33
msB 48 47 46 45 44 43 42 41

Local time :
Second UI6 [1 to 6] <0 to 59> ; 63 : every second (1)
Minute UI6 [9 to 14] <0 to 59> ; 63 : every minute (1)
Hour UI5 [17 to 21] <0 to 23> >; 31 : every hour (1)
Day UI5 [25 to 29] <1 to 31> <0> (0= not specified) (1)
Month UI4 [33 to 36] <1 to 12> <0> 0= not specified (1)
Year UI7 [30 to 32+37 to 40] <0 to 99> <127> 127= not specified (1)
Day of the week UI3 [22 to 24] 1 to 7> 1= Monday 7= Sunday 0= not
specified (3)
Week UI6 [41 to 46] <1 to 53> 0= not specified ( 1)
Time invalid UI1 [16] 1= invalid ; 0 = valid
Time during daylight saving UI1 [7] 1= yes (summer time) ; 0 = no
Leap year UI1 [8] 1= leap year ; 0 = standard year
daylight saving deviation (hour) (2) UI1 [15]
UI2 [47 to 48]
<0 to 1> (1= + 0 = - )
<0 to 3> 0 = no daylight saving
                */
                //e5 00 a0 20 12 20
                //date data received E500A0201220
                //header E5
                //actual date data 00A02012, reversed_data 1220A000, epoch_date 1661126400
                //converted readable date 2022-08-22 00:00:00
                int dst         = Converter.hexToInt(this.fieldParts.get(0)) & 0x40;
                int second      = Converter.hexToInt(this.fieldParts.get(0)) & 0x3F;
                int minutes     = Converter.hexToInt(this.fieldParts.get(1)) & 0x3F;
                int hourOfDay   = Converter.hexToInt(this.fieldParts.get(2)) & 0x3F;
                int date        = Converter.hexToInt(this.fieldParts.get(3)) & 0x1F;
                int month       = (Converter.hexToInt(this.fieldParts.get(4)) & 0x0F) - 1;
                int year1       = Converter.hexToInt(this.fieldParts.get(3)) & 0xE0 ;
                int year2       = Converter.hexToInt(this.fieldParts.get(4)) & 0xF0 ;
                int year        = (year1 >> 5) + (year2 >> 3);

                this.parsedValue = DateCalculator.getDateTimeWithSeconds(this.fieldParts.get(0), this.fieldParts.get(1), this.fieldParts.get(2), this.fieldParts.get(3), this.fieldParts.get(4), false);
                break;
            default:
                return false;
        }
        return true;
    }

    private void parseBCD(int bcd) {
        this.parsedValue = String.valueOf(bcd);
    }

    private void parseBCDAsBigInteger(BigInteger hexToBigInteger) {
        this.parsedValue = hexToBigInteger.toString(10);
    }

    private void parseInteger(BigInteger intValue) {
        this.parsedValue = intValue.toString();
    }


    public TelegramVariableDataRecord getParent() {
        return parent;
    }

    public void setParent(TelegramVariableDataRecord parent) {
        this.parent = parent;
    }

    public void debugOutput() {
        System.out.println("Field-Value (bytes): \t" + this.getFieldPartsAsString());
        System.out.println("Field-Value: \t\t" + this.parsedValue);
    }
}
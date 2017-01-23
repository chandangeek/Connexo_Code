package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 14:29:16
 */
public class DateAndTimeCategory extends AbstractSignedBINObject {

    public DateAndTimeCategory(CTRObjectID id) {
        this.setId(id);
    }

    protected String getSymbol(CTRObjectID id) {
        String symbol = "";

        switch (id.getY()) {
                case 0: switch (id.getZ()) {
                    case 0: symbol = "Date&TimeL"; break;
                    case 1: symbol = "Date&TimeS"; break;
                    case 2: symbol = "Date&TimeP"; break;
                }  break;
                case 1:switch (id.getZ()) {
                    case 2: symbol = "Shift"; break;
                    case 3: symbol = "OFG";  break;
                    case 4: symbol = "Ygas"; break;
                } break;
                case 2: symbol = "OL"; break;
                case 3: symbol = "Data"; break;
                case 4: switch (id.getZ()) {
                    case 0: symbol = "InS"; break;
                    case 1: symbol = "Og_InS"; break;
                } break;
                case 5: symbol = "DSO"; break;

        }
        return symbol;
    }

    public BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit) {
        int overflow = getCommonOverflow(unit);

                int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0:
                    if (valueNumber == 3) {overflow = 8;}
                    if (valueNumber == 7) {overflow = 14;}
                    if (valueNumber == 8) {overflow = 2;}  break;
                case 2: if (valueNumber == 0) {overflow = 0xFF;}  break;
            } break;
            case 1: switch(z) {
                case 2: if (valueNumber == 0) {overflow = 0xFFFF;} break;
                case 4: if (valueNumber == 0) {overflow = 2;} break;
            } break;
            case 2: if (valueNumber == 0) {overflow = 2;} break;
            case 3: if (valueNumber == 5) {overflow = 0xFF;} break;
            case 4: switch(z) {
                case 0: if (valueNumber == 0) {overflow = 360;} break;
            } break;
            case 5: overflow = 0xFFFFFFFF; break;
        }

        return new BigDecimal(overflow);
    }

    public int[] getValueLengths(CTRObjectID id) {
        int[] valueLength = new int[]{};
        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0: valueLength = new int[]{1,1,1,1,1,1,1,1,1}; break;
                case 1: valueLength = new int[]{1,1,1,1,1}; break;
                case 2: valueLength = new int[]{1,1,1,1,1,1}; break;
            } break;
            case 1: switch(z) {
                case 2: valueLength = new int[]{2}; break;
                case 3: valueLength = new int[]{1}; break;
                case 4: valueLength = new int[]{1,1,1,1,1,1,1}; break;
                case 15: valueLength = new int[]{1,1}; break;
            } break;
            case 2: valueLength = new int[]{1,1,1,1,1}; break;
            case 3: valueLength = new int[]{1,1,1,1,1,1}; break;
            case 4: switch(z) {
                case 0: valueLength = new int[]{2}; break;
                case 1: valueLength = new int[]{2,2,2,2,2,2,2,2}; break;
            } break;
            case 5: valueLength = new int[]{3,4,4}; break;
        }
        return valueLength;
    }


    public Unit getUnit(CTRObjectID id, int valueNumber) {
        Unit unit = Unit.get(BaseUnit.UNITLESS);

        int z = id.getZ();
        switch(id.getY()) {
            case 0: switch(z) {
                case 0:
                    if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 5) {unit = Unit.get(BaseUnit.MINUTE);}
                    if (valueNumber == 6) {unit = Unit.get(BaseUnit.SECOND);} break;
                case 1:
                    if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);} break;
                case 2:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 5) {unit = Unit.get(BaseUnit.MINUTE);} break;
            }   break;

            case 1: switch(z) {
                case 2: unit = Unit.get(BaseUnit.SECOND); break;
                case 3: unit = Unit.get(BaseUnit.HOUR); break;
                case 4:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 5) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 6) {unit = Unit.get(BaseUnit.DAY);} break;
            } break;

            case 2:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.DAY);} break;
            case 3:
                    if (valueNumber == 0) {unit = Unit.get(BaseUnit.YEAR);}
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                    if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                    if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);} break;
            case 4:
                    if (z == 0) {unit = Unit.get(BaseUnit.MINUTE);} break;
            case 5:
                    if (valueNumber == 1) {unit = Unit.get(BaseUnit.SECOND);}
                    if (valueNumber == 2) {unit = Unit.get(BaseUnit.SECOND);} break;
        }

        return unit;
    }

    /**
     * Construct a proper java.util.Date out of the value fields
     *
     * @return a Date, expressed in local time zone
     *         WARNING: the produced date is only valid when the device timezone and local timezone are the same!
     */
    @Deprecated
    private Date getDate() throws IndexOutOfBoundsException {
        return getDate(TimeZone.getDefault());
    }

    /**
     * Construct a proper java.util.Date out of the value fields
     *
     * @return a Date, expressed in the given timezone
     */
    public Date getDate(TimeZone timeZone) throws IndexOutOfBoundsException{
        if (getId().is("8.0.1")) {
            Calendar cal = Calendar.getInstance(timeZone);
            cal.set(Calendar.YEAR, getValue(0).getIntValue() + 2000);
            cal.set(Calendar.MONTH, getValue(1).getIntValue() - 1);
            cal.set(Calendar.DAY_OF_MONTH, getValue(2).getIntValue());

            int hour = getValue(3).getIntValue();
            if (hour > 23) {
                hour -= 30;
            }
            cal.set(Calendar.HOUR_OF_DAY, hour);

            int minutes = getValue(4).getIntValue();
            if (minutes > 59) {
               minutes -= 60;
            }
            cal.set(Calendar.MINUTE, minutes);

            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();

        } else if (getId().is("8.0.2")) {
            Calendar cal = Calendar.getInstance(timeZone);
            cal.set(Calendar.YEAR, getValue(1).getIntValue() + 2000);
            cal.set(Calendar.MONTH, getValue(2).getIntValue() - 1);
            cal.set(Calendar.DAY_OF_MONTH, getValue(3).getIntValue());
            cal.set(Calendar.HOUR_OF_DAY, getValue(4).getIntValue());
            cal.set(Calendar.MINUTE, getValue(5).getIntValue());
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();

        } else if (getId().is("8.0.0")) {
            Calendar cal = Calendar.getInstance(timeZone);
            int ptr = 0;
            int year = getValue(ptr++).getIntValue() + 2000;
            int month = getValue(ptr++).getIntValue() - 1;
            int day = getValue(ptr++).getIntValue();
            ptr++; // Day of week
            int hour = getValue(ptr++).getIntValue();
            int min = getValue(ptr++).getIntValue();
            int sec = getValue(ptr++).getIntValue();

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, sec);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();

        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (getId().is("8.1.2")) {   //special case: the residual shift object
            return super.toString();
        } else if (getId().is("8.2.0")) {
            return super.toString();
        }
        Date date = getDate();
        return date != null ? date.toString() : "null";
    }
}

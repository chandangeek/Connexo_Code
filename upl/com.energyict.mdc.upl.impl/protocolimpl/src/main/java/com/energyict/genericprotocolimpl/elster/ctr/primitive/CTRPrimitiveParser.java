package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.protocol.ProtocolUtils;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveParser {

    public CTRPrimitiveParser(){}

    //Parses BIN bytearrays into BigDecimals
    //also parses single byte fields (e.g. hours, minutes,...)
    public CTRObjectValue[] parseBINValue(CTRObjectID id, byte[] rawData, int offset, int[] valueLength) {

        int i = 0;
        CTRObjectValue[] result = new CTRObjectValue[valueLength.length];

        //Parse all given values. Each has its length.
        for(int valueLength1: valueLength) {
            byte[] value = ProtocolUtils.getSubArray(rawData, offset, offset + valueLength1 - 1);
            value = removeTrailingZeroes(value, valueLength1);
            result[i] = new CTRObjectValue(parseOverflowValue(id, i), parseUnit(id, i), convertByteArrayToBigDecimal(value));
            i++;
            offset += valueLength1;
        }
        return result;  //Array of all value objects, each with its unit & domain.
    }

    public String parseSymbol(CTRObjectID id) {
        String symbol = "";

        if (id.getX() == 1) {       // Flow or volume category

            if (id.getY() == 0){        //Spec is measured flow

                if (id.getZ() == 0) {symbol = "Qm";}
                if (id.getZ() == 1) {symbol = "Qm_15";}
                if (id.getZ() == 2) {symbol = "Qm_1h";}
                if (id.getZ() == 3) {symbol = "Qmc";}
            }

            if (id.getY() == 1){        //Spec is measured volume

                if (id.getZ() == 3) {symbol = "Vm_g";}
                if (id.getZ() == 4) {symbol = "Vm_m";}
                if (id.getZ() == 5) {symbol = "Vm_1y";}
            }

        }

        return symbol;
    }

    public Unit parseUnit(CTRObjectID id, int valueNumber) {
        Unit unit = null;
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        // Category: flow or volume
        if (x == 0x01) {
            if ((y == 0x01) || (y == 0x03) || (y >= 0x0D)) {
                unit = Unit.get("m3");
            } else {
                unit = Unit.get("m3/h");
                if (y == 0x06 || y == 0x07 || y == 0x09 || y == 0x0A) {
                    if (z == 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.DAY);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 3) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                    if (z <= 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                    if (z > 0x04) {
                        if (valueNumber == 1) {unit = Unit.get(BaseUnit.MONTH);}
                        if (valueNumber == 2) {unit = Unit.get(BaseUnit.DAY);}
                        if (valueNumber == 3) {unit = Unit.get(BaseUnit.HOUR);}
                        if (valueNumber == 4) {unit = Unit.get(BaseUnit.MINUTE);}
                    }
                }
            }
        }

        //Category: Totalizers
        if (x == 0x02) {
            unit = Unit.get("m3");
        }

        //Category: Energy
        if (x == 0x03) {
            unit = Unit.get(BaseUnit.JOULE, 6);
            if (y == 0x00) {unit = Unit.get(BaseUnit.JOULEPERHOUR, 6);}
        }

        //Category: Pressure
        if (x == 0x04) {
            unit = Unit.get(BaseUnit.BAR);
            if (y == 0x03 || y == 0x04 || y == 0x06 || y == 0x07) {
                if (valueNumber == 1) {unit = Unit.get(BaseUnit.HOUR);}
                if (valueNumber == 2) {unit = Unit.get(BaseUnit.MINUTE);}
            }
        }

        if (x == 0x05) {
            unit = Unit.get(BaseUnit.BAR);
            if (y >= 0x02) {
                if (valueNumber == 1) {unit = Unit.get(BaseUnit.HOUR);}
                if (valueNumber == 2) {unit = Unit.get(BaseUnit.MINUTE);}
            }
        }

        if (x == 0x06) {
            unit = Unit.get(BaseUnit.BAR);
        }

        //Category: Temperature
        if (x == 0x07) {
            if (y <= 0x06) {
                unit = Unit.get(BaseUnit.KELVIN);
            }
        }

        return unit;
    }

    public CTRObjectID parseId(byte[] data, int offset) {

        byte byte1 = data[offset];
        byte byte2 = data[offset + 1];
        int x,y,z;
        x = ((int)(byte1)) & 0xFF;
        y = ((int)(byte2 >> 4)) & 0xFF;
        z = ((int)(byte2 << 4)) & 0xFF;
        z = ((int)(z >> 4)) & 0xFF;
        CTRObjectID ctrObjectId = new CTRObjectID(x,y,z);

        return ctrObjectId;
    }

    public int parseQlf(byte[] rawData, int offset) {
        int Qlf = ((int)rawData[offset]) & 0xFF;
        return Qlf;
    }

    public int parseAccess(byte[] rawData, int offset) {
        int Access = ((int)rawData[offset]) & 0xFF;
        return Access;
    }

    //One Id can contain multiple values (each with its proper overflowValue)
    public BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber) {
        BigDecimal overflowValue = null;
        if (parseUnit(id, valueNumber) == Unit.get(BaseUnit.MONTH)) {
            overflowValue = new BigDecimal(13);
        }
        if (parseUnit(id, valueNumber) == Unit.get(BaseUnit.DAY)) {
            overflowValue = new BigDecimal(32);
        }
        if (parseUnit(id, valueNumber) == Unit.get(BaseUnit.HOUR)) {
            overflowValue = new BigDecimal(24);
        }
        if (parseUnit(id, valueNumber) == Unit.get(BaseUnit.MINUTE)) {
            overflowValue = new BigDecimal(60);
        }


        return overflowValue;
    }

    private BigDecimal convertByteArrayToBigDecimal(byte[] value) {
        long convertedValue = 0;
        int len = value.length;
        for(int i = 0;i <len-1; i++) {
            byte Byte = value[i];
            convertedValue += ((int) ((Byte & 0xFF) << i*8) );
        }
        BigDecimal result = BigDecimal.valueOf(convertedValue);
        return result;
    }

    private byte[] removeTrailingZeroes(byte[] value, int len) {
        while(value[len-1] == 0x00) { len--; }
        value = ProtocolUtils.getSubArray(value, 0, len - 1);
        return value;
    }

    public int[] parseValueLength(CTRObjectID id) {
        int[] valueLength = null;
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        switch (x) {
            case 1: switch(y) {
                default: valueLength = new int[]{3}; break;
                case 6:
                case 7:
                case 9:
                case 0x0A:
                    valueLength = new int[]{3,1,1};
                    switch (z) {
                        case 4: valueLength = new int[]{3,1,1,1}; break;
                        case 5:
                        case 6: valueLength = new int[]{3,1,1,1,1}; break;
                    }
            }

            case 2: valueLength = new int[]{4}; break;

            case 3: switch(y) {
                default: valueLength = new int[]{4};
                case 0: valueLength = new int[]{3}; break;
            }

            case 4: switch(y) {
                default: valueLength = new int[]{3};
                case 4:
                case 6:
                case 7:
                case 3: valueLength = new int[]{3,1,1}; break;
                case 9: switch(z) {
                    case 0: valueLength = new int[]{3,3,3,3,3}; break;
                    default: valueLength = new int[]{3}; break;
                }
            }

            case 7: switch(y) {
                case 9: switch(z) {
                    default: valueLength = new int[]{3};
                    case 0x0A: valueLength = new int[]{2,2}; break;
                }
                case 0x0B: switch(z) {
                    default: valueLength = new int[]{3};
                    case 0: valueLength = new int[]{3,3,3,3,3,3}; break;
                }

                case 0x0C: valueLength = new int[]{3,3}; break;

                case 0: valueLength = new int[]{3}; break;
                case 6:
                case 3: valueLength = new int[]{3,1,1}; break;
            }

            case 8: switch(y) {
                case 0: switch(z) {
                    case 0: valueLength = new int[]{1,1,1,1,1,1,1,1,1}; break;
                    case 1: valueLength = new int[]{1,1,1,1,1}; break;
                    case 2: valueLength = new int[]{1,1,1,1,1,1}; break;
                }
                case 1: switch(z) {
                    case 2: valueLength = new int[]{2}; break;
                    case 3: valueLength = new int[]{1}; break;
                    case 4: valueLength = new int[]{1,1,1,1,1,1,1}; break;
                }
                case 2: valueLength = new int[]{1,1,1,1,1}; break;
                case 3: valueLength = new int[]{1,1,1,1,1,1}; break;
                case 4: switch(z) {
                    case 0: valueLength = new int[]{2}; break;
                    case 1: valueLength = new int[]{2,2,2,2,2,2,2,2}; break;
                }
                case 5: valueLength = new int[]{3,4,4}; break;
            }

            case 9: switch(y) {
                case 0: switch(z) {
                    case 0: valueLength = new int[]{12,5,4,6,3,4,1}; break;
                    case 1: valueLength = new int[]{12}; break;
                    case 2: valueLength = new int[]{5}; break;
                    case 3: valueLength = new int[]{4}; break;
                    case 4: valueLength = new int[]{6}; break;
                    case 5: valueLength = new int[]{3}; break;
                    case 7: valueLength = new int[]{4}; break;
                    case 9: valueLength = new int[]{1}; break;

                }
                case 1: switch(z) {
                    case 1:
                    case 2: valueLength = new int[]{1,2}; break;
                    case 3: valueLength = new int[]{1,1}; break;
                }
                case 2: switch(z) {
                    case 0: valueLength = new int[]{16}; break;
                    case 2: valueLength = new int[]{4}; break;
                    case 3: valueLength = new int[]{1}; break;
                    case 4: valueLength = new int[]{2}; break;
                    case 5: valueLength = new int[]{4,4,3}; break;
                }
                case 3: valueLength = new int[]{10}; break;
                case 4: switch(z) {
                    case 0: valueLength = new int[]{2}; break;
                    case 1: valueLength = new int[]{6}; break;
                }
            }

            case 0x0A: valueLength = new int[]{3}; break;

            case 0x0B: switch(y) {
                default: valueLength = new int[]{3};
                case 0: valueLength = new int[]{3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3}; break;
            }

            case 0x0C: switch(y) {
                case 0: switch(z) {
                    case 0: valueLength = new int[]{7}; break;
                    case 1:
                    case 3:
                    case 2: valueLength = new int[]{1}; break;
                    case 4: valueLength = new int[]{60}; break;
                    case 5: valueLength = new int[]{3,3,2}; break;
                    case 6:
                    case 7: valueLength = new int[]{30}; break;
                }
                case 1: valueLength = new int[]{10,10,16}; break;
                case 2: switch(z) {
                    case 0: valueLength = new int[]{4,3,13}; break;
                    case 1: valueLength = new int[]{4,16}; break;
                }
            }

            case 0x0D: switch(y) {
                case 6: valueLength = new int[]{8}; break;
                case 7: valueLength = new int[]{6}; break;
                case 8: valueLength = new int[]{16}; break;
                case 9: valueLength = new int[]{2}; break;
                case 10: valueLength = new int[]{4}; break;
            }

            case 0x0E: switch(y) {
                case 0x0C: valueLength = new int[]{1}; break;
                case 0x0E: valueLength = new int[]{112}; break;
            }

            case 0x0F: switch(y) {
                default: valueLength = new int[]{1,1,1,1,1};
                case 5: valueLength = new int[]{3,3,3,3}; break;  //def value = 6 ipv 12  !!?
            }

            case 0x10: switch(y) {
                case 0: switch(z) { 
                    default: valueLength = new int[]{1,1,1,1,1,2,1,1,4,4}; break;
                    case 3: valueLength = new int[]{1,1,1,1,1,1,1,1,2,1,4,1,4,1,4,1,4,1,4,1,4}; break;
                }
                case 1: valueLength = new int[]{2}; break;
                case 2: valueLength = new int[]{2}; break;
                case 3: valueLength = new int[]{1}; break;
            }

            case 0x11: switch(y) {
                case 0: switch(z) {
                    case 1: valueLength = new int[]{1,1,1,1,1,1,1,1,1,1}; break;
                    case 8: valueLength = new int[]{1}; break; 
                }
            }

            case 0x12: switch(y) {
                case 0: valueLength = new int[]{1}; break;
                case 1: valueLength = new int[]{4}; break;
                case 2: valueLength = new int[]{2}; break;
                case 4: valueLength = new int[]{1,2}; break;
                case 5: valueLength = new int[]{2,2}; break;
            }

            case 0x15: valueLength = new int[]{1,32}; break;
            case 0x18: valueLength = new int[]{4}; break;

            
                
        }

        return valueLength;
    }
}

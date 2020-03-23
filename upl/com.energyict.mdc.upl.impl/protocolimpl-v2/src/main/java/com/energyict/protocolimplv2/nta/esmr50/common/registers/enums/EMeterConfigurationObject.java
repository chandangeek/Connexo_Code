package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Structure;

import java.util.Iterator;

/**
 * Created by iulian on 8/27/2016.
 */
public class EMeterConfigurationObject {

    private static final String SEP = ";\n";
    private String errorMessage="";
    private boolean decoded;
    private String description;
    private int flagsValue;

    public EMeterConfigurationObject(AbstractDataType abstractDataType) {

        try {
            if (abstractDataType.isStructure()){
                Structure structure = abstractDataType.getStructure();
                BitString flags = structure.getNextDataType().getBitString();
                if (flags == null){
                    if (structure.hasMoreElements()){
                        flags = structure.getNextDataType().getBitString();
                    }
                }
                if (flags!=null) {
                    description = decodeFlags(flags);
                    flagsValue = flags.intValue();
                    decoded = true;
                } else {
                    errorMessage = "Unknown structure received (not a bit string inside)";
                }
            } else {
                errorMessage = "Data is not a structure: "+abstractDataType.toString();
            }
        } catch (Exception ex){
            decoded = false;
            errorMessage = ex.getMessage();
        }
    }

    private String decodeFlags(BitString flags) {
        StringBuilder sb = new StringBuilder();
        int b = flags.getNrOfBits()-1;
        Iterator<Boolean> iterator = flags.iterator();
        while (b >= 0){
            boolean bit = flags.get(b);

            switch (b){
                case 0: if (bit) {  sb.append("b0: discover_on_open_cover").append(SEP);  }     break;
                case 1: if (bit) {  sb.append("b1: discover_on_power_on").append(SEP);    } break;
                case 2: if (bit) {  sb.append("b2: dynamic_mbus_address").append(SEP);       }break;
                case 3: if (bit) {  sb.append("b3: P0_enable").append(SEP);    }  break;
                case 4: if (bit) {  sb.append("b4: HLS_3_on_P0 and P3_enable").append(SEP);      } break;
                case 5: if (bit) {  sb.append("b5: HLS_4_on_P0 and P3_enable").append(SEP);    } break;
                case 6: if (bit) {  sb.append("b6: HLS_5_on_P0 and P3_enable").append(SEP);    } break;
                case 7: if (bit) {  sb.append("b7: HLS_3_on_P0_enable").append(SEP);       }break;
                case 8: if (bit) {  sb.append("b8: HLS_4_on_P0_enable").append(SEP);      }break;
                case 9: if (bit) {  sb.append("b9: HLS_5_on_P0_enable").append(SEP);   }  break;
                case 10: if (bit) {  sb.append("b10: (bit 10)").append(SEP);    }  break;//NOT USED
                case 11: if (bit) {  sb.append("b11: LTE_postponing_enable").append(SEP);    }  break;
            }

            b--;
        }

        return sb.toString();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isDecoded() {
        return decoded;
    }

    public String toString() {
        return description;
    }

    public int getFlags() {
        return flagsValue;
    }
}

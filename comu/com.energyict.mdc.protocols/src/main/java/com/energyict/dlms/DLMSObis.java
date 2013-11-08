/*
 * DLMSObis.java
 *
 * Created on 7 januari 2003, 14:14
 */

package com.energyict.dlms;

import com.energyict.dlms.cosem.DLMSClassId;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author  Koen
 */
public class DLMSObis {

    byte[] tokens=null;
    String str;
    short offset;
    short dlmsclass;


    public DLMSObis(byte[] ln, short classId, short offset) {
        this.tokens=ln;
        this.dlmsclass=classId;
        this.offset=offset;
    }
    /** Creates a new instance of DLMSObis */
    public DLMSObis(String str) throws IOException {
        this.str = str;

        if (str.charAt(0) == '#') {
			tokens = null;
		} else {
            StringTokenizer st = new StringTokenizer(str,":");
            int i = st.countTokens();

            dlmsclass = 0;
            offset = 0;
            tokens = null;
            if (i==3) {
                tokens = parseLNTokens(st.nextToken());
                dlmsclass = Short.parseShort(st.nextToken());
                offset = Short.parseShort(st.nextToken());

            }
            else if (i==2) {
                tokens = parseLNTokens(st.nextToken());
                dlmsclass = Short.parseShort(st.nextToken());
                //offset = Short.parseShort(st.nextToken());

            } else if (i==1) {
                tokens = parseLNTokens(st.nextToken());
                //offset=1;
            }
        }
    }

    private byte[] parseLNTokens(String str) {
        StringTokenizer st = new StringTokenizer(str,".");
        int i = st.countTokens();
        if  (i == 6) {
            byte[] tkns = new byte[6];
            for (i=0;i<tkns.length;i++) {
                tkns[i] = (byte)Integer.parseInt(st.nextToken());
            }
            return tkns;
        }

        return null;
    }

    public byte[] getLN() {
        return tokens;
    }

    public short getOffset() {
      return offset;
    }

    public short getDLMSClass() {
       return dlmsclass;
    }

    public boolean isLogicalName() {
       return (tokens!=null );
    }

    public int getVal(int index) throws IOException {
        if (isLogicalName()) {
            return (int)tokens[index]&0xFF;
        } else {
			throw new IOException("No valid logical name "+str);
		}
    }

    public int getLNA() throws IOException {
        return getVal(0);
    }

    public int getLNB() throws IOException {
        return getVal(1);
    }

    public int getLNC() throws IOException {
        return getVal(2);
    }

    public int getLND() throws IOException {
        return getVal(3);
    }

    public int getLNE() throws IOException {
        return getVal(4);
    }

    public int getLNF() throws IOException {
        return getVal(5);
    }

    public int getValueAttributeOffset() throws IOException {
        if (getDLMSClass() == DLMSClassId.REGISTER.getClassId()) {
			return 8;
		} else if (getDLMSClass() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
			return 8;
		} else if (getDLMSClass() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
			return 16; // last average value
		} else if (getDLMSClass() == DLMSClassId.DATA.getClassId()) {
			return 8;
		} else {
			throw new IOException("UniversalObject, wrong object for value attribute!");
		}
    }
    public int getScalerAttributeOffset() throws IOException {
        if (getDLMSClass() == DLMSClassId.REGISTER.getClassId()) {
			return 16;
		} else if (getDLMSClass() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
			return 16;
		} else if (getDLMSClass() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
			return 24;
		} else if (getDLMSClass() == DLMSClassId.DATA.getClassId()) {
			return 8;
		} else {
			throw new IOException("UniversalObject, wrong object for scaler attribute!");
		}
    }

    public String toString() {
        try {
            return getLNA()+"."+
                   getLNB()+"."+
                   getLNC()+"."+
                   getLND()+"."+
                   getLNE()+"."+
                   getLNF()+", classId "+
                   getDLMSClass()+", attribute "+
                   getOffset();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args)
    {

        try {
            DLMSObis ln = new DLMSObis("1.1.82.8.0.255:3");
            System.out.println(ln.getLNA()+"."+
                               ln.getLNB()+"."+
                               ln.getLNC()+"."+
                               ln.getLND()+"."+
                               ln.getLNE()+"."+
                               ln.getLNF()+":"+
                               ln.getDLMSClass()+":"+
                               ln.getOffset());
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

}

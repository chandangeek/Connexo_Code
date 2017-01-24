/*
 * DLMSObis.java
 *
 * Created on 7 januari 2003, 14:14
 */

package com.energyict.dlms;

import com.energyict.mdc.protocol.api.ProtocolException;

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
    public DLMSObis(String str) {
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

    public int getVal(int index) throws ProtocolException {
        if (isLogicalName()) {
            return (int)tokens[index]&0xFF;
        } else {
			throw new ProtocolException("No valid logical name "+str);
		}
    }

    public int getLNA() throws ProtocolException {
        return getVal(0);
    }

    public int getLNB() throws ProtocolException {
        return getVal(1);
    }

    public int getLNC() throws ProtocolException {
        return getVal(2);
    }

    public int getLND() throws ProtocolException {
        return getVal(3);
    }

    public int getLNE() throws ProtocolException {
        return getVal(4);
    }

    public int getLNF() throws ProtocolException {
        return getVal(5);
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

}
package com.energyict.dlms.client;

import java.io.IOException;
import java.math.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocol.ProtocolUtils;

public class NumberFormat {
	long value;
	int exponent;
	
	public NumberFormat(AbstractDataType dataType) {
		parse(dataType);
	}
	
	public NumberFormat(long value, int exponent) {
		this.value = value;
		this.exponent = exponent;
	}
	
	public NumberFormat(Number number) throws IOException {
		parse(number);
	}
	
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("NumberFormat:\n");
        strBuff.append("   exponent="+getExponent()+"\n");
        strBuff.append("   value="+getValue()+"\n");
        strBuff.append("   toBigDecimal="+toBigDecimal().toPlainString()+"\n");
        return strBuff.toString();
    }
    
	private int findPosExponent(Long val) {
		int posExp=1;
		int exponent=0;
		// find positive exp
		while(exponent < 18) {
			if ((val%(long)Math.pow(10,posExp)) == 0)
				exponent=posExp++;
			else {
				break;
			}
		}
		return exponent;
	}
	
	private void parse(Number number) throws IOException {
		String val = number.toString().replace(".","");
		try {
			value = Long.parseLong(val);
		}
		catch(NumberFormatException e) {
			throw new IOException("Cannot parse "+val+" to long value!");
		}
		if (value != 0) {
			String[] vals = number.toString().split("\\.");
			if (vals.length==2) {
				exponent = vals[1].length()*(-1);
			}
			int exponent2=findPosExponent(value);
			value /= (long)Math.pow(10,exponent2);
			exponent+=exponent2;
		}
	}
	
	private void parse(AbstractDataType dataType) {
		if (dataType.isOctetString()) {
			byte[] data = dataType.getOctetString().getOctetStr();
			exponent = (int)data[0];
			if ((data.length-1) == 1) {
				value = (long)data[1];
			}
			else if ((data.length-1) == 2) {
				value = (long)ProtocolUtils.getShort(data, 1);
			}
			else if ((data.length-1) == 4) {
				value = (long)ProtocolUtils.getInt(data, 1);
			}
			else if ((data.length-1) == 8) {
				value = (long)ProtocolUtils.getLong(data, 1);
			}
		}
		else {
			exponent=0;
			value = dataType.longValue();
		}
	}
    
    public BigDecimal toBigDecimal() {
    	return new BigDecimal(BigInteger.valueOf(value),exponent*(-1));
    }
    
    public AbstractDataType toAbstractDataType() throws IOException {
    	
    	AbstractDataType temp; 
		// code value as int 8, 16 or 32
		if ((value <= 127) && (value >= -128)) {
			temp = new Integer8((int)value);
		}
		else if ((value <= 32767) && (value >= -32768)) {
			temp = new Integer16((short)value);
		}
		else if ((value <= 2147483647L) && (value >= -2147483648L)) {
			temp = new Integer32((int)value);
		}
		else temp = new Integer64((long)value);
		
       	if (exponent == 0) {
       		return temp;
    	}
    	else {
    		// code exponent as signed byte first byte of octetstring
    		// code value in 1..8 bytes in octetstring
    		// tricky...
    		byte[] data = temp.getBEREncodedByteArray();
    		// and now replace encoding byte with exponent!
    		data[0] = (byte)exponent;
    		temp = new OctetString(data);
    		return temp;
    	}
    	
    	
    }
    
	public long getValue() {
		return value;
	}
	public int getExponent() {
		return exponent;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		System.out.println(new NumberFormat(new BigDecimal("1123.456")));
//		System.out.println(new NumberFormat(new BigInteger("123654987")));
//		System.out.println(new NumberFormat(new Long(456)));
//		System.out.println(new NumberFormat(new Integer(400560000)));
//		System.out.println(new NumberFormat(0));
//		System.out.println(new NumberFormat(new BigDecimal("456.")));
//		System.out.println(new NumberFormat(new BigDecimal("1")));
//		System.out.println(new NumberFormat(new BigDecimal("100")));
//		System.out.println(new NumberFormat(new BigDecimal("100100")));
		
		
		try {
			//NumberFormat o = new NumberFormat(new BigDecimal("2772123123.123300078"));
			NumberFormat o = new NumberFormat(new BigDecimal("12345678.0002"));
			System.out.println(o);
			System.out.println(ProtocolUtils.outputHexString(o.toAbstractDataType().getBEREncodedByteArray()));
			System.out.println(o.toAbstractDataType());
			NumberFormat o2 = new NumberFormat(o.toAbstractDataType());
			System.out.println(o2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
}

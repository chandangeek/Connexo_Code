/*
 * DataContainer.java
 *
 * Created on 3 april 2003, 17:16
 */

package com.energyict.dlms;
import java.io.IOException;
import java.io.Serializable;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.Utils;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class DataContainer implements DLMSCOSEMGlobals, Serializable {
	private int iLevel=0;
	private int iMaxLevel=0;
	private int iIndex=0;
	private DataStructure dataStructure=null;
	private DataStructure dataStructureRoot=null;

	public DataContainer() {
		this.dataStructure=null;
		this.dataStructureRoot=null;
		this.iLevel=0;
		this.iIndex=0;
		this.iMaxLevel=0;
	}

	public void printDataContainer() {
		System.out.println(doPrintDataContainer());
	}

	@Override
	public String toString() {
		return doPrintDataContainer();
	}

	public String print2strDataContainer() {
		return doPrintDataContainer();
	}

	public String getText(String delimiter) {
		DataStructure dataStructure;
		int iparseLevel=0,i;
		StringBuffer strout = new StringBuffer();

		int[] iLength = new int[getMaxLevel()+1];
		int[] iCount = new int[getMaxLevel()+1];
		for (i=0;i<getMaxLevel()+1;i++)
		{
			iLength[i] = 0;
			iCount[i] = -1;
		}
		iLength[iparseLevel] = getRoot().element.length;
		dataStructure = getRoot();
		strout.append("DC("+iLength[iparseLevel]+")"+delimiter);
		do
		{
			while (++iCount[iparseLevel] < iLength[iparseLevel]) {
				for (i=0;i<iparseLevel;i++) {
					//strout.append("        ");
					if (i==(iparseLevel-1)) {
						strout.append(" ("+iparseLevel+"/"+(iCount[iparseLevel]+1)+") ");
					}
				}
				if (dataStructure.isLong(iCount[iparseLevel])) {
					strout.append(dataStructure.getLong(iCount[iparseLevel])+delimiter);
				}

				if (dataStructure.isInteger(iCount[iparseLevel])) {
					strout.append(dataStructure.getInteger(iCount[iparseLevel])+delimiter);
				}

				if (dataStructure.isOctetString(iCount[iparseLevel])) {
					int val;
					boolean readable=true;
					for (int t=0; t<dataStructure.getOctetString(iCount[iparseLevel]).getArray().length;t++) {
						val = dataStructure.getOctetString(iCount[iparseLevel]).getArray()[t]&0xFF;
						if ((val < 0x20) || (val > 0x7E)) {
							readable = false;
						}
						strout.append(ProtocolUtils.outputHexString(val));
						strout.append(" ");
					}

					if (readable) {
						String str = new String(dataStructure.getOctetString(iCount[iparseLevel]).getArray());
						strout.append(str);
					}

					if (dataStructure.getOctetString(iCount[iparseLevel]).getArray().length == 6) {
						strout.append("("+DLMSUtils.getInfoLN(dataStructure.getOctetString(iCount[iparseLevel]).getArray())+")");
					}


					strout.append(delimiter);

				}

				if (dataStructure.isString(iCount[iparseLevel])) {
					strout.append(dataStructure.getString(iCount[iparseLevel])+delimiter);
				}

				if (dataStructure.isStructure(iCount[iparseLevel])) {
					strout.append("S("+dataStructure.getStructure(iCount[iparseLevel]).element.length+")"+delimiter);
					iparseLevel++;
					iLength[iparseLevel] = dataStructure.getStructure(iCount[iparseLevel-1]).element.length;
					dataStructure = dataStructure.getStructure(iCount[iparseLevel-1]);
				}

			} // while (++iCount[iparseLevel] < iLength[iparseLevel])

			dataStructure = dataStructure.parent;
			iCount[iparseLevel] = -1;
			iLength[iparseLevel] = 0;

		} while (iparseLevel-- > 0);

		return strout.toString();

	} // getText()


	public String doPrintDataContainer() {
		DataStructure dataStructure;
		int iparseLevel=0,i;
		StringBuffer strout = new StringBuffer();

		int[] iLength = new int[getMaxLevel()+1];
		int[] iCount = new int[getMaxLevel()+1];
		for (i=0;i<getMaxLevel()+1;i++)
		{
			iLength[i] = 0;
			iCount[i] = -1;
		}
		iLength[iparseLevel] = getRoot().element.length;
		dataStructure = getRoot();
		strout.append("DataContainer with "+iLength[iparseLevel]+" elements\n");
		do
		{
			while (++iCount[iparseLevel] < iLength[iparseLevel]) {
				for (i=0;i<iparseLevel;i++) {
					strout.append("        ");
					if (i==(iparseLevel-1)) {
						strout.append(" ("+iparseLevel+"/"+(iCount[iparseLevel]+1)+") ");
					}
				}
				if (dataStructure.isLong(iCount[iparseLevel])) {
					strout.append("Long: ");
					strout.append(dataStructure.getLong(iCount[iparseLevel])+"\n");
				}

				if (dataStructure.isInteger(iCount[iparseLevel])) {
					strout.append("Integer: ");
					strout.append(dataStructure.getInteger(iCount[iparseLevel])+"\n");
				}

				if (dataStructure.isOctetString(iCount[iparseLevel])) {
					strout.append("OctetString: ");
					int val;
					boolean readable=true;
                    OctetString octetString = dataStructure.getOctetString(iCount[iparseLevel]);
                    for (int t=0; t< octetString.getArray().length;t++) {
						val = octetString.getArray()[t]&0xFF;
						if ((val < 0x20) || (val > 0x7E)) {
							readable = false;
						}
						strout.append(ProtocolUtils.outputHexString(val));
						strout.append(" ");
					}

					if (readable) {
						String str = new String(octetString.getArray());
						strout.append(str);
					}

					if (octetString.getArray().length == 6) {
						strout.append(" "+DLMSUtils.getInfoLN(octetString.getArray()));
					}

                    if (octetString.getArray().length == 12) {
                        strout.append(" Date="+octetString.toDate());
                    }

					strout.append("\n");

				}

				if (dataStructure.isString(iCount[iparseLevel])) {
					strout.append("String: ");
					strout.append(dataStructure.getString(iCount[iparseLevel])+"\n");
				}

				if (dataStructure.isStructure(iCount[iparseLevel])) {
					strout.append("Struct with "+dataStructure.getStructure(iCount[iparseLevel]).element.length+" elements.\n");
					iparseLevel++;
					iLength[iparseLevel] = dataStructure.getStructure(iCount[iparseLevel-1]).element.length;
					dataStructure = dataStructure.getStructure(iCount[iparseLevel-1]);
				}

			} // while (++iCount[iparseLevel] < iLength[iparseLevel])

			dataStructure = dataStructure.parent;
			iCount[iparseLevel] = -1;
			iLength[iparseLevel] = 0;

		} while (iparseLevel-- > 0);

		return strout.toString();
	}
	public int getMaxLevel()
	{
		return this.iMaxLevel;
	}

	public DataStructure getRoot() {
		return this.dataStructureRoot;
	}

	private void levelDown() throws DataContainerException {
		while (true) {
			if (this.iLevel > 0) {
				this.iLevel--;
			}
			if (this.dataStructure.parent != null) {
				this.dataStructure = this.dataStructure.parent;
				// Get next index...
				for (this.iIndex = 0; this.iIndex < this.dataStructure.element.length; this.iIndex++) {
					if (this.dataStructure.element[this.iIndex] == null) {
						return;
					}
				}
				if ((this.iIndex == this.dataStructure.element.length) && (this.iLevel == 0)) {
					throw new DataContainerException("LevelDown error, no more free entries!");
				}
			} else {
				throw new DataContainerException("LevelDown error, no parent!");
			}
		}
	}

	private void prepare() throws DataContainerException {
		if (this.dataStructure == null)
		{
			this.dataStructure = new DataStructure(1);
			this.dataStructure.parent = null;
			this.dataStructureRoot=this.dataStructure;
			this.iLevel=0;
			this.iMaxLevel = this.iLevel;
			this.iIndex=0;
		} else if (this.iIndex >= this.dataStructure.element.length) {
			levelDown();
		}
	}

	public void addLong(long value) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = new Long(value);
	}

	public void addInteger(int iValue) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = new Integer(iValue);
	}

	public void addString(String str) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = str;
	}

	public void addOctetString(byte[] array) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = new OctetString(array);
	}

	public void addStructure(int iNROfElements) throws DataContainerException {
		if (this.dataStructure == null)
		{
			this.dataStructure = new DataStructure(iNROfElements);
			this.dataStructure.parent = null;
			this.dataStructureRoot=this.dataStructure;
			this.iLevel=0;
			this.iMaxLevel = this.iLevel;
			this.iIndex=0;
		}
		else
		{
			if (this.iIndex >= this.dataStructure.element.length) {
				levelDown();
			}

			this.dataStructure.element[this.iIndex] = new DataStructure(iNROfElements);
			((DataStructure)this.dataStructure.element[this.iIndex]).parent = this.dataStructure;
			this.dataStructure = (DataStructure)this.dataStructure.element[this.iIndex];
			this.iLevel++;
			if (this.iLevel > this.iMaxLevel) {
				this.iMaxLevel = this.iLevel;
			}
			this.iIndex=0;

		}
	}


	public void parseObjectList(byte[] responseData, Logger logger) throws IOException {
		//DataContainer dataContainer= new DataContainer();
		doParseObjectList(responseData,logger);
	}

	private static final int MAX_LEVELS=20;
	private void doParseObjectList(byte[] responseData, Logger logger) throws IOException {
		int i=0,temp;
		int iLevel=0;
		int[] LevelNROfElements = new int[MAX_LEVELS];
		for (temp=0;temp<20;temp++)
		{
			LevelNROfElements[temp]=0;
		}
		if(responseData != null){

		while(true) {
			try {
					
					switch (responseData[i])
					{
					case TYPEDESC_ARRAY:
					{
						i++;
						if (iLevel++ >= (MAX_LEVELS-1)) {
							throw new IOException("Max printlevel exceeds!");
						}
						
						LevelNROfElements[iLevel] = (int)DLMSUtils.getAXDRLength(responseData,i);
						addStructure(LevelNROfElements[iLevel]);
						i += DLMSUtils.getAXDRLengthOffset(responseData,i);
						
						
					} break; // TYPEDESC_ARRAY
					
					case TYPEDESC_STRUCTURE:
					{
						i++;
						if (iLevel++ >= (MAX_LEVELS-1)) {
							throw new IOException("Max printlevel exceeds!");
						}
						LevelNROfElements[iLevel] = (int)DLMSUtils.getAXDRLength(responseData,i);
						addStructure(LevelNROfElements[iLevel]);
						i += DLMSUtils.getAXDRLengthOffset(responseData,i);
						
					} break; // TYPEDESC_STRUCTURE
					
					case TYPEDESC_NULL:
					{
						i++;
						addInteger(0);
					} break;
					
					case TYPEDESC_LONG:
					case TYPEDESC_LONG_UNSIGNED:
					{
						i++;
						addInteger(ProtocolUtils.getShort(responseData,i));
						i+=2;
						
					} break;
					
					case TYPEDESC_VISIBLE_STRING:
					case TYPEDESC_OCTET_STRING:
					{
						int t,s;
						i++;
						t = (int)DLMSUtils.getAXDRLength(responseData,i);
						byte[] array = new byte[t];
						i += DLMSUtils.getAXDRLengthOffset(responseData,i);
						for (s=0;s<t;s++) {
							array[s] = responseData[i+s];
						}
						addOctetString(array);
						i += t;
					} break;
					
					case TYPEDESC_DOUBLE_LONG:
					case TYPEDESC_DOUBLE_LONG_UNSIGNED:
					{
						i++;
						addInteger(ProtocolUtils.getInt(responseData,i));
						i+=4;
					} break;
					
					case TYPEDESC_BCD:
					case TYPEDESC_ENUM:
					case TYPEDESC_INTEGER:
					case TYPEDESC_BOOLEAN:
					case TYPEDESC_UNSIGNED:
					{
						i++;
						addInteger(responseData[i]&0xFF);
						i++;
					} break;
					
					
					case TYPEDESC_LONG64:
					{
						i++;
						addLong(ProtocolUtils.getLong(responseData,i));
						i+=8;
					} break;
					
					case TYPEDESC_BITSTRING:
					{
						int t,s;
						i++;
						t = (int)DLMSUtils.getAXDRLength(responseData,i);
						i += DLMSUtils.getAXDRLengthOffset(responseData,i);
						
						// calc nr of bytes
						if ((t%8) == 0) {
							t = (t/8);
						} else {
							t = ((t/8)+1);
						}
						
						int iValue=0;
						for (s=0;s<t;s++) {
							iValue += ((responseData[i+s]&0xff)<<(s*8));
						}
						addInteger(iValue);
						
						i+=t;
						
					} break; // TYPEDESC_BITSTRING
					
					case TYPEDESC_FLOATING_POINT:
					{
						i++;
						addInteger(0); // TODO
						i+=4;
						
					} break; // TYPEDESC_FLOATING_POINT
					
					case TYPEDESC_TIME:
					{
						i++;
						addInteger(0); // TODO
						i+=4; // ??? generalizedTime
						
					} break; // TYPEDESC_TIME
					
					case TYPEDESC_COMPACT_ARRAY:
					{
						i++;
						addInteger(0); // TODO
					} break; // TYPEDESC_COMPACT_ARRAY
					
					default:
					{
						i++;
					} break;
					}
					
					while(true)
					{
						if (--LevelNROfElements[iLevel] <0)
						{
							if (--iLevel < 0)
							{
								iLevel=0;
								break;
							}
						} else {
							break;
						}
					}
				}
				catch(DataContainerException e) {
					if (logger == null) {
						System.out.println(Utils.stack2string(e)+", probably meter data corruption! Datablock contains more elements than the axdr data encoding!");
					} else {
						logger.severe(Utils.stack2string(e)+", probably meter data corruption! Datablock contains more elements than the axdr data encoding!");
					}
					return;
				}
				
				if (i>=responseData.length) {
					return;
				}
				
			} // while(true)
		} else {
			return;
		}

	} //  void parseObjectList(byte[] responseData)


	public static void main(String[] args) {
		try {
			DataContainer dc = new DataContainer();

			//		   byte b[] = new byte[]{(byte)0x1, (byte)0x4, (byte)0x2, (byte)0x2, (byte)0x0, (byte)0x12, (byte)0x80, (byte)0x11, (byte)0x2, (byte)0x2,
			//				   (byte)0x9, (byte)0x0C, (byte)0x7, (byte)0xD9, (byte)0x2, (byte)0x12, (byte)0x3, (byte)0x0F, (byte)0x2B, (byte)0x2D, (byte)0x0,
			//				   (byte)0xFF, (byte)0xC4, (byte)0x0, (byte)0x12, (byte)0x80, (byte)0x11, (byte)0x2, (byte)0x2, (byte)0x9, (byte)0x0C, (byte)0x7,
			//				   (byte)0xD9, (byte)0x2, (byte)0x12, (byte)0x3, (byte)0x0F, (byte)0x30, (byte)0x23, (byte)0x0, (byte)0xFF, (byte)0xC4, (byte)0x0,
			//				   (byte)0x12, (byte)0x0, (byte)0x80, (byte)0x2, (byte)0x2, (byte)0x9, (byte)0x0C, (byte)0x7, (byte)0xD9, (byte)0x2, (byte)0x12,
			//				   (byte)0x3, (byte)0x0F, (byte)0x30, (byte)0x26, (byte)0x0, (byte)0xFF, (byte)0xC4, (byte)0x0, (byte)0x12, (byte)0x0, (byte)0x40};
			//
			//		   dc.parseObjectList(b, null);
			//
			//		   dc.printDataContainer();

			byte by[] = DLMSUtils.hexStringToByteArray("010d0204090c07d80915070c0f0000ff88801120060000000006000000000204090c07d8091507152d0000ff88801120060000000006000000000204090c07d9020403152d0000ffc4001180060000000006000000000204090c07d902050408000000ffc4001180060000000006000000000204090c07d9020a0207000000ffc40011c00600000000060000000002040011e006000001d40600000000020400110006000001de06000000000204001100060000062a060000000002040011000600000770060000000202040011000600000770060000000a0204001100060000077006000003c9020400110006000007700600000565020400118006000007700600000565");
			dc = new DataContainer();
			dc.parseObjectList(by, null);
			dc.printDataContainer();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

} // class DataContainer


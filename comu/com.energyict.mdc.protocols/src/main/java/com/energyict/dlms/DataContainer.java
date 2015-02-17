/*
 * DataContainer.java
 *
 * Created on 3 april 2003, 17:16
 */

package com.energyict.dlms;

import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
/**
 *
 * @author  Koen
 */
public class DataContainer implements Serializable {
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
		StringBuilder strout = new StringBuilder();

		int[] iLength = new int[getMaxLevel()+1];
		int[] iCount = new int[getMaxLevel()+1];
		for (i=0;i<getMaxLevel()+1;i++)
		{
			iLength[i] = 0;
			iCount[i] = -1;
		}
		iLength[iparseLevel] = getRoot().element.length;
		dataStructure = getRoot();
		strout.append("DC(").append(iLength[iparseLevel]).append(")").append(delimiter);
		do
		{
			while (++iCount[iparseLevel] < iLength[iparseLevel]) {
				for (i=0;i<iparseLevel;i++) {
					//strout.append("        ");
					if (i==(iparseLevel-1)) {
						strout.append(" (").append(iparseLevel).append("/").append(iCount[iparseLevel] + 1).append(") ");
					}
				}
				if (dataStructure.isLong(iCount[iparseLevel])) {
					strout.append(dataStructure.getLong(iCount[iparseLevel])).append(delimiter);
				}

				if (dataStructure.isInteger(iCount[iparseLevel])) {
					strout.append(dataStructure.getInteger(iCount[iparseLevel])).append(delimiter);
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
						strout.append("(").append(DLMSUtils.getInfoLN(dataStructure.getOctetString(iCount[iparseLevel]).getArray())).append(")");
					}


					strout.append(delimiter);

				}

				if (dataStructure.isString(iCount[iparseLevel])) {
					strout.append(dataStructure.getString(iCount[iparseLevel])).append(delimiter);
				}

				if (dataStructure.isStructure(iCount[iparseLevel])) {
					strout.append("S(").append(dataStructure.getStructure(iCount[iparseLevel]).element.length).append(")").append(delimiter);
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
		StringBuilder strout = new StringBuilder();

		int[] iLength = new int[getMaxLevel()+1];
		int[] iCount = new int[getMaxLevel()+1];
		for (i=0;i<getMaxLevel()+1;i++)
		{
			iLength[i] = 0;
			iCount[i] = -1;
		}
		iLength[iparseLevel] = getRoot().element.length;
		dataStructure = getRoot();
		strout.append("DataContainer with ").append(iLength[iparseLevel]).append(" elements\n");
		do
		{
			while (++iCount[iparseLevel] < iLength[iparseLevel]) {
				for (i=0;i<iparseLevel;i++) {
					strout.append("        ");
					if (i==(iparseLevel-1)) {
						strout.append(" (").append(iparseLevel).append("/").append(iCount[iparseLevel] + 1).append(") ");
					}
				}
				if (dataStructure.isLong(iCount[iparseLevel])) {
					strout.append("Long: ");
					strout.append(dataStructure.getLong(iCount[iparseLevel])).append("\n");
				}

				if (dataStructure.isInteger(iCount[iparseLevel])) {
					strout.append("Integer: ");
					strout.append(dataStructure.getInteger(iCount[iparseLevel])).append("\n");
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
						strout.append(" ").append(DLMSUtils.getInfoLN(octetString.getArray()));
					}

                    if (octetString.getArray().length == 12) {
                        strout.append(" Date=").append(octetString.toDate());
                    }

					strout.append("\n");

				}

				if (dataStructure.isString(iCount[iparseLevel])) {
					strout.append("String: ");
					strout.append(dataStructure.getString(iCount[iparseLevel])).append("\n");
				}

				if (dataStructure.isStructure(iCount[iparseLevel])) {
					strout.append("Struct with ").append(dataStructure.getStructure(iCount[iparseLevel]).element.length).append(" elements.\n");
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

    public void addFloat(float Value) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = new Float(Value);
	}

	public void addString(String str) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = str;
	}

	public void addOctetString(byte[] array) throws DataContainerException {
		prepare();
		this.dataStructure.element[this.iIndex++] = new OctetString(array);
	}

    public float getFloat32(byte[] byteBuffer, int iOffset) throws DataContainerException {
        int signBit = (((int) byteBuffer[iOffset] >> 7) & 0xFF);
        int exponent = (((((int) byteBuffer[iOffset]) << 1) & 0xFF) |
                ((((int) byteBuffer[iOffset + 1]) >> 7) & 0x01));

        int fractionDigits = ((((int) byteBuffer[iOffset + 1] << 16) & 0x7F0000) |
                (((int) byteBuffer[iOffset + 2] << 8) & 0x00FF00) |
                (((int) byteBuffer[iOffset + 3]) & 0x0000FF));
        String fractionPart = Integer.toBinaryString(fractionDigits);

        // fractionPart should be length 23 -- the leading 0's should be present in the string!
        while (fractionPart.length() < 23) {
            fractionPart = "0" + fractionPart;
        }

        float fraction = 1;
        for (int i = 0; i < 23; i++) {
            fraction += Integer.parseInt(fractionPart.substring(i, i + 1)) * Math.pow(2, -1 - i);
        }
        if (exponent > 0) {
            return ((float) ((Math.pow(-1, signBit)) * Math.pow(2, exponent - 127)) * fraction);
        }
        else {
            return 0;
        }
    }

    public float getFloat64(byte[] byteBuffer, int iOffset) throws DataContainerException {
        int signBit = (((int) byteBuffer[iOffset] >> 7) & 0xFF);
        long exponent = (((((long) byteBuffer[iOffset]) << 4) & 0x07FF) |
                ((((long) byteBuffer[iOffset + 1]) >> 4) & 0x0F));

        long fractionDigits = ((((long) byteBuffer[iOffset + 1] & 0x0F) << 48) |
                ((((long) byteBuffer[iOffset + 2]) & 0xFF) << 40) |
                ((((long) byteBuffer[iOffset + 3]) & 0xFF) << 32) |
                ((((long) byteBuffer[iOffset + 4]) & 0xFF) << 24) |
                ((((long) byteBuffer[iOffset + 5]) & 0xFF) << 16) |
                ((((long) byteBuffer[iOffset + 6]) & 0xFF) << 8) |
                ((((long) byteBuffer[iOffset + 7]) & 0xFF)));
        String fractionPart = Long.toBinaryString(fractionDigits);

        // fractionPart should be length 52 -- the leading 0's should be present in the string!
        while (fractionPart.length() < 52) {
            fractionPart = "0" + fractionPart;
        }

        float fraction = 1;
        for (int i = 0; i < 52; i++) {
            fraction += Integer.parseInt(fractionPart.substring(i, i + 1)) * Math.pow(2, -1 - i);
        }
        if (exponent > 0) {
            return ((float) ((Math.pow(-1, signBit)) * Math.pow(2, exponent - 1023)) * new Float(fraction));
        }
        else {
            return 0;
        }
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


	public void parseObjectList(byte[] responseData, Logger logger) {
		doParseObjectList(responseData,logger);
	}

	private static final int MAX_LEVELS=20;
	protected void doParseObjectList(byte[] responseData, Logger logger) {
		int i=0,temp;
		int iLevel=0;
		int[] LevelNROfElements = new int[MAX_LEVELS];
		for (temp=0;temp<20;temp++){
			LevelNROfElements[temp]=0;
		}
		if(responseData != null){

		while(true) {
			try {

                AxdrType axdrType = AxdrType.fromTag(responseData[i]);
                switch (axdrType)
					{
					case ARRAY:
					{
						i++;
						if (iLevel++ >= (MAX_LEVELS-1)) {
                            IOException ioException = new IOException("Max printlevel exceeds!");
                            throw new CommunicationException(MessageSeeds.GENERAL_PARSE_ERROR, ioException);
						}

						LevelNROfElements[iLevel] = (int)DLMSUtils.getAXDRLength(responseData,i);
						addStructure(LevelNROfElements[iLevel]);
						i += DLMSUtils.getAXDRLengthOffset(responseData,i);


					} break; // TYPEDESC_ARRAY

					case STRUCTURE:
					{
						i++;
						if (iLevel++ >= (MAX_LEVELS-1)) {
                            IOException ioException = new IOException("Max printlevel exceeds!");
                            throw new CommunicationException(MessageSeeds.GENERAL_PARSE_ERROR, ioException);
						}
						LevelNROfElements[iLevel] = (int)DLMSUtils.getAXDRLength(responseData,i);
						addStructure(LevelNROfElements[iLevel]);
						i += DLMSUtils.getAXDRLengthOffset(responseData,i);

					} break; // TYPEDESC_STRUCTURE

					case NULL:
					{
						i++;
						addInteger(0);
					} break;

					case LONG:
					case LONG_UNSIGNED:
					{
						i++;
						addInteger(ProtocolUtils.getShort(responseData,i));
						i+=2;

					} break;

					case VISIBLE_STRING:
					case OCTET_STRING:
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

					case DOUBLE_LONG:
					case DOUBLE_LONG_UNSIGNED:
					{
						i++;
						addInteger(ProtocolUtils.getInt(responseData,i));
						i+=4;
					} break;

					case BCD:
					case ENUM:
					case INTEGER:
					case BOOLEAN:
					case UNSIGNED:
					{
						i++;
						addInteger(responseData[i]&0xFF);
						i++;
					} break;


					case LONG64:
                    case LONG64_UNSIGNED:
					{
						i++;
						addLong(ProtocolUtils.getLong(responseData,i));
						i+=8;
					} break;

					case BIT_STRING:
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

					case FLOATING_POINT:
					{
						i++;
						addInteger(0); // TODO
						i+=4;

					} break; // TYPEDESC_FLOATING_POINT

					case TIME:
					{
						i++;
						addInteger(0); // TODO
						i+=4; // ??? generalizedTime

					} break; // TYPEDESC_TIME

					case COMPACT_ARRAY:
					{
						i++;
						addInteger(0); // TODO
					} break; // TYPEDESC_COMPACT_ARRAY

                    case FLOAT32:
                    {
                        i++;
                        addFloat(getFloat32(responseData, i));
                        i += 4;
                    } break;

                    case FLOAT64:
                    {
                        i++;
                        addFloat(getFloat64(responseData, i));
                        i += 8;
                    } break;

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
						System.out.println(ProtocolUtils.stack2string(e)+", probably meter data corruption! Datablock contains more elements than the axdr data encoding!");
					} else {
						logger.severe(() -> ProtocolUtils.stack2string(e) + ", probably meter data corruption! Datablock contains more elements than the axdr data encoding!");
					}
					return;
				}

				if (i>=responseData.length) {
					return;
				}

			}
		} else {
			return;
		}

	}

}


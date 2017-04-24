/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataContainer.java
 *
 * Created on 3 april 2003, 17:16
 */

package com.energyict.protocolimpl.edf.trimarandlms.axdr;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
/**
 *
 * @author  Koen
 */
public class TrimaranDataContainer implements TrimaranDLMSCOSEMGlobals, Serializable {

    /**	Generated serialVersionUID */
	private static final long serialVersionUID = 8516323547596564299L;

	protected int iLevel=0;
    protected int iMaxLevel=0;
    protected int iIndex=0;
    protected TrimaranDataStructure dataStructure=null;
    protected TrimaranDataStructure dataStructureRoot=null;

    public TrimaranDataContainer() {
        dataStructure=null;
        dataStructureRoot=null;
        iLevel=0;
        iIndex=0;
        iMaxLevel=0;
    }

    public void printDataContainer() {
        System.out.println(doPrintDataContainer());
    }

    public String toString() {
        return doPrintDataContainer();
    }

    public String print2strDataContainer() {
        return doPrintDataContainer();
    }

    public String getText(String delimiter) {
        TrimaranDataStructure dataStructure;
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
                     if (i==(iparseLevel-1)){
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
                        val = (int)dataStructure.getOctetString(iCount[iparseLevel]).getArray()[t]&0xFF;
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
						strout.append("(").append(TrimaranDLMSUtils.getInfoLN(dataStructure.getOctetString(iCount[iparseLevel]).getArray())).append(")");
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
        TrimaranDataStructure dataStructure;
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
        strout.append("TrimaranDataContainer with ").append(iLength[iparseLevel]).append(" elements\n");
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
					strout.append(dataStructure.getLong(iCount[iparseLevel])).append("\n");
				}

                if (dataStructure.isInteger(iCount[iparseLevel])) {
					strout.append(dataStructure.getInteger(iCount[iparseLevel])).append("\n");
				}

                if (dataStructure.isOctetString(iCount[iparseLevel])) {
                    int val;
                    boolean readable=true;
                    for (int t=0; t<dataStructure.getOctetString(iCount[iparseLevel]).getArray().length;t++) {
                        val = (int)dataStructure.getOctetString(iCount[iparseLevel]).getArray()[t]&0xFF;
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
						strout.append(" ").append(TrimaranDLMSUtils.getInfoLN(dataStructure.getOctetString(iCount[iparseLevel]).getArray()));
					}


                    strout.append("\n");

                }

                if (dataStructure.isString(iCount[iparseLevel])) {
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
        return iMaxLevel;
    }

    public TrimaranDataStructure getRoot() {
        return dataStructureRoot;
    }

    private void levelDown() throws TrimaranDataContainerException {
        while(true) {
            if (iLevel > 0) {
				iLevel--;
			}
            if (dataStructure.parent != null)
            {
               dataStructure=dataStructure.parent;
               // Get next index...
               for (iIndex=0;iIndex<dataStructure.element.length;iIndex++) {
				if (dataStructure.element[iIndex] == null) {
					return;
				}
			}
               if ((iIndex==dataStructure.element.length) && (iLevel == 0)) {
				throw new TrimaranDataContainerException("LevelDown error, no more free entries!");
			}
            } else {
				throw new TrimaranDataContainerException("LevelDown error, no parent!");
			}
        }
    }

    private void prepare() throws TrimaranDataContainerException {
        if (dataStructure == null)
        {
           dataStructure = new TrimaranDataStructure(1);
           dataStructure.parent = null;
           dataStructureRoot=dataStructure;
           iLevel=0;
           iMaxLevel = iLevel;
           iIndex=0;
        }
        else if (iIndex >= dataStructure.element.length) {
            levelDown();
        }
    }

    public void addLong(long value) throws TrimaranDataContainerException {
        prepare();
        dataStructure.element[iIndex++] = new Long(value);
    }

    public void addInteger(int iValue) throws TrimaranDataContainerException {
        prepare();
        dataStructure.element[iIndex++] = new Integer(iValue);
    }

    public void addString(String str) throws TrimaranDataContainerException {
        prepare();
        dataStructure.element[iIndex++] = str;
    }

    public void addOctetString(byte[] array) throws TrimaranDataContainerException {
        prepare();
        dataStructure.element[iIndex++] = new TrimaranOctetString(array);
    }

    public void addStructure(int iNROfElements) throws TrimaranDataContainerException {
        if (dataStructure == null)
        {
           dataStructure = new TrimaranDataStructure(iNROfElements);
           dataStructure.parent = null;
           dataStructureRoot=dataStructure;
           iLevel=0;
           iMaxLevel = iLevel;
           iIndex=0;
        }
        else
        {
           if (iIndex >= dataStructure.element.length) {
			levelDown();
		}

           dataStructure.element[iIndex] = new TrimaranDataStructure(iNROfElements);
           ((TrimaranDataStructure)dataStructure.element[iIndex]).parent = dataStructure;
           dataStructure = (TrimaranDataStructure)dataStructure.element[iIndex];
           iLevel++;
           if (iLevel > iMaxLevel) {
			iMaxLevel = iLevel;
		}
           iIndex=0;

        }
    }


   public void parseObjectList(byte[] responseData, Logger logger) throws IOException {
       //TrimaranDataContainer dataContainer= new TrimaranDataContainer();
       doParseObjectList(responseData,logger);
   }

   private static final int MAX_LEVELS=20;
   private void doParseObjectList(byte[] responseData, Logger logger) throws IOException {
       int i=0,temp;
       int iLevel=0;
       int[] LevelNROfElements = new int[MAX_LEVELS];
       for (temp=0;temp<20;temp++){
           LevelNROfElements[temp]=0;
       }

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

                       LevelNROfElements[iLevel] = (int) TrimaranDLMSUtils.getAXDRLength(responseData, i);
                       addStructure(LevelNROfElements[iLevel]);
                       i += TrimaranDLMSUtils.getAXDRLengthOffset(responseData, i);


                   } break; // TYPEDESC_ARRAY

                   case TYPEDESC_STRUCTURE:
                   {
                       i++;
                       if (iLevel++ >= (MAX_LEVELS-1)) {
						throw new IOException("Max printlevel exceeds!");
					}
                       LevelNROfElements[iLevel] = (int) TrimaranDLMSUtils.getAXDRLength(responseData, i);
                       addStructure(LevelNROfElements[iLevel]);
                       i += TrimaranDLMSUtils.getAXDRLengthOffset(responseData, i);

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
                       addInteger((int)ProtocolUtils.getShort(responseData,i));
                       i+=2;

                   } break;

                   case TYPEDESC_VISIBLE_STRING:
                   case TYPEDESC_OCTET_STRING:
                   {
                       int t,s;
                       i++;
                       t = (int) TrimaranDLMSUtils.getAXDRLength(responseData, i);
                       byte[] array = new byte[t];
                       i += TrimaranDLMSUtils.getAXDRLengthOffset(responseData, i);
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
                       addLong(ProtocolUtils.getLong(responseData,i,4));
                       i+=4;
                   } break;

                   case TYPEDESC_BCD:
                   case TYPEDESC_ENUM:
                   case TYPEDESC_INTEGER:
                   case TYPEDESC_BOOLEAN:
                   case TYPEDESC_UNSIGNED:
                   {
                       i++;
                       addInteger((int)responseData[i]&0xFF);
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
                      t = (int) TrimaranDLMSUtils.getAXDRLength(responseData, i);
                      i += TrimaranDLMSUtils.getAXDRLengthOffset(responseData, i);

                      // calc nr of bytes
                      if ((t%8) == 0) {
						t = (t/8);
					} else {
						t = ((t/8)+1);
					}

                      long value=0;
                      for (s=0;s<t;s++) {
						value += (((long)responseData[i+(t-1)-s]&0xff)<<(s*8));
					}
                      addLong(value);

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
           catch(TrimaranDataContainerException e) {
               if (logger == null) {
				System.out.println(ProtocolUtils.stack2string(e)+", probably meter data corruption! Datablock contains more elements than the axdr data encoding!");
			} else {
				logger.severe(ProtocolUtils.stack2string(e)+", probably meter data corruption! Datablock contains more elements than the axdr data encoding!");
			}
               return;
           }

           if (i>=responseData.length) {
               return;
           }

       }

   }

}


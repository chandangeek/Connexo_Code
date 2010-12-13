/*
 * CapturedObjects.java
 *
 * Created on 3 april 2003, 17:25
 */

package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * @deprecated  As of 11022009, replaced by
 * com.energyict.dlms.cosem.CaptureObjectsHelper
 *
 */
public class CapturedObjects implements DLMSCOSEMGlobals
{
   LNObject[] lnObject;
   int iNROfChannels;

   public CapturedObjects(int iNROfObjects)
   {
      lnObject = new LNObject[iNROfObjects];
      iNROfChannels=0;
   }
   public int getNROfObjects()
   {
       return lnObject.length;
   }
   public int getNROfChannels()
   {
       return iNROfChannels;
   }
   public int getType(int iIndex)
   {
       return lnObject[iIndex].getType();
   }
   public boolean isChannelData(int iIndex)
   {
       return (lnObject[iIndex].getType()==CHANNEL_DATA);
   }
   public byte[] getLN(int iIndex)
   {
       return lnObject[iIndex].getLogicalName();
   }

   public ObisCode getProfileDataChannel(int channelId) throws IOException {
       int id=0;
       for(int i=0;i<getNROfObjects();i++) {
           if (isChannelData(i)) {
               if (id == channelId) {
				return new ObisCode(lnObject[i].getLogicalName()[0]&0xff,lnObject[i].getLogicalName()[1]&0xff,lnObject[i].getLogicalName()[2]&0xff,lnObject[i].getLogicalName()[3]&0xff,lnObject[i].getLogicalName()[4]&0xff,lnObject[i].getLogicalName()[5]&0xff);
			}
               id++;
           }
       }
       throw new IOException("CapturedObjects, invalid channelId "+channelId);

   } // public ObisCode getProfileDataChannel(int channelId) throws IOException

   public byte[] getChannelLN(int iChannelNR)
   {
       for(int i=0;i<getNROfObjects();i++)
       {
          if (lnObject[i].getChannelId() == iChannelNR) {
			return lnObject[i].getLogicalName();
		}
       }

       throw new java.lang.ArrayIndexOutOfBoundsException("CaptureObjects getChannelLN error!");
   }

   public LNObject getChannelObject(int iChannelNR)
   {
       for(int i=0;i<getNROfObjects();i++)
       {
          if (lnObject[i].getChannelId() == iChannelNR) {
			return lnObject[i];
		}
       }
       throw new java.lang.ArrayIndexOutOfBoundsException("CaptureObjects getChannelObject error!");
   }

   public int getChannelNR(int index)
   {
       if (index < lnObject.length)
       {
          return lnObject[index].getChannelId();
       } else {
		throw new java.lang.ArrayIndexOutOfBoundsException("CaptureObjects class add error!");
	}
   }

   public void add(int iIndex, int iIC,byte[] ln,int iAttr)
   {
       if (iIndex < lnObject.length)
       {
          // Changed KV 22052003 to read also gas puls channels!
          if ((ln[LN_A]  != 0) && //== LN_A_ELECTRICITY_RELATED_OBJECTS) &&
              (ln[LN_B] >= 0) && // was 1 (KV 06032007)
              (ln[LN_B] <= 64) &&
              ((iIC == DLMSClassId.REGISTER.getClassId()) || (iIC == DLMSClassId.DEMAND_REGISTER.getClassId()))) {
              lnObject[iIndex] = new LNObject(iIC,ln,iAttr,CHANNEL_DATA, iNROfChannels);
              iNROfChannels++;
          }


          // Changed GN 29022008 to add the extended register for the Iskra MBus meter
          else if( ((ln[LN_A] == 0)||((ln[LN_A]) == 7)) &&
        		  (ln[LN_B] == 1) &&
        		  (ln[LN_C] == (byte)0x80) &&
        		  (ln[LN_D] == 50) &&
        		  (ln[LN_E] >= 0) && (ln[LN_E] <= 3) &&
        		  (iIC == DLMSClassId.EXTENDED_REGISTER.getClassId())){
              lnObject[iIndex] = new LNObject(iIC,ln,iAttr,CHANNEL_DATA, iNROfChannels);
              iNROfChannels++;
          }

          else {
              lnObject[iIndex] = new LNObject(iIC,ln,iAttr,MANUFACTURER_SPECIFIC_DATA, -1);
          }
       } else {
		throw new java.lang.ArrayIndexOutOfBoundsException("CaptureObjects class add error!");
	}
   }

} // class CapturedObjects


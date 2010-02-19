package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

/**
 * @author jme
 *
 */
public class GProfileBuilder {

	private final GasDevice	as220;
	private final CapturedObjectsHelper coh;
	private static final long maskValidity  = 0x80000000;
	private static final long maskEncrypted = 0x40000000;
	

	public GProfileBuilder(GasDevice as220, CapturedObjectsHelper coh) {
		this.as220 = as220;
		this.coh = coh;
	}

	private GasDevice getGasDevice() {
		return as220;
	}

	/**
	 * @param coh
	 * @return
	 * @throws IOException
	 */
	public ScalerUnit[] buildScalerUnits() throws IOException {
		ScalerUnit[] scalerUnits = new ScalerUnit[coh.getNrOfchannels()];
		for (int i = 0; i < scalerUnits.length; i++) {
	        ObisCode obisCode = coh.getProfileDataChannelObisCode(i);
//	        scalerUnits[i] = getGasDevice().getCosemObjectFactory().getGenericRead(getGasDevice().getCorrectedChannelObisCode(obisCode), (byte)0x08, 4).getScalerUnit();
	        scalerUnits[i] = new ScalerUnit(Unit.get(BaseUnit.LITER));

		}
		return scalerUnits;
	}

	/**
	 * Build a list of {@link ChannelInfo}s
	 *  
	 * @param scalerunit
	 * 				- an array of ScalerUnits
	 * 
	 * @return a list of ChannelInfos
	 * 
	 * @throws IOException
	 */
	public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit){
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		for (int i = 0; i < scalerunit.length; i++) {
			ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getGasDevice().getDeviceID() + "_channel_" + i, scalerunit[i].getUnit());
			channelInfo.setCumulative();
			channelInfos.add(channelInfo);
		}
		return channelInfos;
	}

	/**
	 * Build a list of {@link IntervalData}
	 * 
	 * @param scalerunit
	 * 				- an array of ScalerUnits
	 * @param dc 
	 * 				- a {@link DataContainer} with the profileData from the device
	 * 
	 * @return a list a IntervalData
	 * @throws UnsupportedException
	 * @throws IOException
	 */
	public List<IntervalData> buildIntervalData(ScalerUnit[] scalerunit, DataContainer dc ) throws UnsupportedException, IOException {
		
		
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){
		
			for(int i = 0; i < dc.getRoot().getElements().length; i++){
				
				//Test
				if(dc.getRoot().getStructure(i) == null){
					dc.printDataContainer();
					System.out.println("Element: " + i);
				}
				
				if(dc.getRoot().getStructure(i).isOctetString(0)){	// it is a date
					cal = new DateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(0).getArray()), getGasDevice().getTimeZone()).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, getGasDevice().getProfileInterval());	// TODO
					}
				}
				
				if(cal != null){		
					
					profileStatus = GasStatusCodes.intervalStateBits(dc.getRoot().getStructure(i).getInteger(1));
						
					int value = dc.getRoot().getStructure(i).getInteger(2);
					if((value&maskValidity) == maskValidity){
						value = (int) (value^maskValidity);
						profileStatus |= IntervalStateBits.MISSING;
					} else if((value&maskEncrypted) == maskEncrypted){
						value = (int) (value^maskEncrypted);
					} else if((value&maskEncrypted) == 0){
						profileStatus |= IntervalStateBits.CORRUPTED;	// if it is not an encrypted value 
					}
					IntervalData id = new IntervalData(cal.getTime(), profileStatus);
					id.addValue(value);
					
					intervalDatas.add(id);
				}
			}
		} else {
			getGasDevice().getLogger().info("No entries in LoadProfile");
		}
		return intervalDatas;
	}
	
	public static void main(String args[]){
		byte[] b = new byte[]{1, 45, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 9, 28, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 11, 22, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 12, 40, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 13, 33, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 14, 8, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 14, 33, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 14, 49, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 14, 59, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 20, 1, 0, -128, 0, 0, 6, 0, 0, -64, -1, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 21, 38, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 25, 32, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 27, 7, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 28, 8, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 28, 50, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 29, 21, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 29, 41, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 29, 53, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 35, 0, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 36, 39, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 40, 33, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 42, 8, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 43, 11, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 43, 53, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 44, 21, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 44, 42, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 44, 56, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 52, 3, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 54, 44, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 56, 33, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 57, 48, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 58, 38, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 59, 13, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 59, 35, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 15, 59, 50, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 3, 5, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 8, 16, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 10, 33, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 12, 6, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 13, 10, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 13, 52, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 14, 21, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 14, 42, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 14, 55, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 18, 5, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, 0, 0, 0, -116, 2, 3, 9, 12, 7, -38, 2, 17, 4, 16, 23, 17, 0, -128, 0, 0, 6, 0, 0, -64, 0, 6, -128, 0, 0, 0};
		
		try {
			GProfileBuilder builder = new GProfileBuilder(null, null);
			
			int bit = GasStatusCodes.intervalStateBits(49407);
			System.out.println(bit);
			
			
			DataContainer dc = new DataContainer();
			dc.parseObjectList(b, Logger.getAnonymousLogger());
//			
			builder.buildIntervalData(null, dc);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

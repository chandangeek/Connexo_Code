package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.CapturedObjectsHelper;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.dlms.as220.GasDevice;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author jme
 *
 */
public class GProfileBuilder {

	private final GasDevice	as220;
	private final CapturedObjectsHelper coh;
	private static final long MASK_VALIDITY     = 0x80000000;
	private static final long MASK_ENCRYPTED    = 0x40000000;
    private static final long MASK_VALUE        = 0x3FFFFFFF;


	public GProfileBuilder(GasDevice as220, CapturedObjectsHelper coh) {
		this.as220 = as220;
		this.coh = coh;
	}

	private GasDevice getGasDevice() {
		return as220;
	}

	/**
	 * Builder for the scalerUnits.
	 *
	 * Currently we hardcoded the scalerUnit as a Liter
	 *
	 * @return a ScalerUnit array of 1 element, being Liter
	 *
	 * @throws IOException
	 */
	public ScalerUnit[] buildScalerUnits() throws IOException {
		ScalerUnit[] scalerUnits = new ScalerUnit[coh.getNrOfchannels()];
		for (int i = 0; i < scalerUnits.length; i++) {
//	        ObisCode obisCode = coh.getProfileDataChannelObisCode(i);
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
	 */
	@SuppressWarnings("deprecation")
	public List<ChannelInfo> buildChannelInfos(ScalerUnit[] scalerunit) {
		List<ChannelInfo> channelInfos = new ArrayList<>();
		for (int i = 0; i < scalerunit.length; i++) {
			ChannelInfo channelInfo = new ChannelInfo(i, "dlms" + getGasDevice().getDeviceID() + "_channel_" + i, scalerunit[i].getEisUnit());

			// Setting the cumulative value is the old way of doing, Eandis has an old way of doing so ...
			channelInfo.setCumulativeWrapValue(new BigDecimal(100000000));
            // the setCumulative() is only from 8.5
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
	 * @throws IOException
	 */
	public List<IntervalData> buildIntervalData(ScalerUnit[] scalerunit, DataContainer dc ) throws IOException {
        int capturePeriod = getGasDevice().getgMeter().getMbusProfile().getCapturePeriod();

		List<IntervalData> intervalDatas = new ArrayList<>();
		Calendar cal = null;
		int profileStatus;
		if(dc.getRoot().getElements().length != 0){

			for(int i = 0; i < dc.getRoot().getElements().length; i++){

                if(dc.getRoot().getStructure(i) != null){
                    if(dc.getRoot().getStructure(i).isOctetString(0)){	// it is a date
                        cal = new DateTime(OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(0).getArray()), getGasDevice().getTimeZone()).getValue();
                    } else {
                        if(cal != null){
                            cal.add(Calendar.SECOND, capturePeriod);
                        }
                    }

                    if(cal != null){

                        profileStatus = GasStatusCodes.intervalStateBits(dc.getRoot().getStructure(i).getInteger(1));

                        int value = dc.getRoot().getStructure(i).getInteger(2);

                        if (isBitSet(value, MASK_VALIDITY)) {
                            profileStatus = IntervalStateBits.MISSING;
                        }
                        if (!isBitSet(value, MASK_ENCRYPTED)) {
                            profileStatus |= IntervalStateBits.CORRUPTED;
                        }

                        value &= MASK_VALUE;

                        if (ProtocolTools.isCorrectIntervalBoundary(cal, capturePeriod)) {
                            IntervalData id = new IntervalData(cal.getTime(), profileStatus);
                            id.addValue(MBusValueTranslator.interpret(value, getGasDevice().getDif()));
                            intervalDatas.add(id);
                        } else {
                            getGasDevice().getLogger().severe("Removing interval from profile data [" + cal.getTime() + " = " + value + "]. Not on interval boundary.");
                        }
                    }

                } else {
                    getGasDevice().getLogger().info("GasProfile contained a 'NULL' structure.");
                }

			}
		} else {
			getGasDevice().getLogger().info("No entries in LoadProfile");
		}
		return intervalDatas;
	}

    private boolean isBitSet(int value, long mask) {
        return (value & mask) == mask;
    }

}

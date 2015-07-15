package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySetting;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySettingFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Historical2LogReader extends AbstractLogReader {



	private List<LinePoint> meterlpMap;
	private List<LinePoint> masterlpMap;
	private ScaledEnergySettingFactory sesf;

	public Historical2LogReader(OutputStream os ,NexusProtocolConnection npc, List<LinePoint> mtrlpMap, List<LinePoint> mstrlpMap, ScaledEnergySettingFactory sesf) {
		this.sesf = sesf;
		meterlpMap = mtrlpMap;
		masterlpMap = mstrlpMap;
		outputStream = os;
		connection = npc;
		windowIndexAddress = new byte[] {(byte) 0x95, 0x01};;  
		windowModeAddress = new byte[] {(byte) 0x95, 0x41};
		windowEndAddress = 38400;
	}

	@Override
	protected Command getHeaderCommand() {
		return NexusCommandFactory.getFactory().getHistorical2LogHeaderCommand();
	}

	@Override
	protected Command getWindowCommand() {
		return NexusCommandFactory.getFactory().getHistorical2LogWindowCommand();
	}

	protected byte[] readLogHeader() throws IOException {
		Command command = NexusCommandFactory.getFactory().getHistorical2LogHeaderCommand();
		outputStream.write(command.build());
		return connection.receiveWriteResponse(command).toByteArray();

	}


	@Override
	public void parseLog(byte[] byteArray, ProfileData profileData, Date from, int intervalSeconds) throws IOException {
		List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
		int offset = 0;
		int length = 4;
		int recNum = 0;
		

		try{

			while (offset < byteArray.length) {
				int eiStatus = 0;
				length=8;
				Date recDate = parseF3(byteArray, offset);
				Calendar cal = Calendar.getInstance();
				cal.setTime(recDate);
				ParseUtils.isOnIntervalBoundary(cal, intervalSeconds);
				int rest = (int)(cal.getTime().getTime()/1000) % intervalSeconds;
				if (rest!=0) { 
					if (rest < 450 ) {
						ParseUtils.roundDown2nearestInterval(cal, intervalSeconds);
					}
					else {
						ParseUtils.roundUp2nearestInterval(cal, intervalSeconds);
					}
					recDate = cal.getTime();
					eiStatus = IntervalStateBits.SHORTLONG;
				}
				
//				System.out.println(recDate + " --- " + cal.getTime());
				if (recDate.before(from)) {
					recNum++;
					offset = recNum * recordSize;
					continue;
				}
                                Calendar cal2 = Calendar.getInstance();
				cal2.add(Calendar.DATE, 1);
				if (recDate.after(cal2.getTime())) {
					recNum++;
					offset = recNum * recordSize;
					continue;
				}


				
				offset+= length;
				IntervalData intervalData = new IntervalData(recDate,0,0);
				for (LinePoint lp : meterlpMap) {
					boolean found = false;
					for (LinePoint lp2 : masterlpMap) {
						
						if (lp.getLine() == lp2.getLine() && lp.getPoint() == lp2.getPoint()) {
							found = true;
							BigDecimal val =new BigDecimal( parseF64(byteArray, offset));
							offset+=4;
							BigDecimal divisor = new BigDecimal(1);
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
							}
							val = val.divide(divisor, MathContext.DECIMAL128);
							intervalData.addValue(val, 0, eiStatus);
							break;
						}
					}
					if (!found) {
						offset+=4;
					}
				}

				recNum++;
				offset = recNum * recordSize;//meterlpMap.size()*8;
				intervalDatas.add(intervalData);
			}
		}catch (Exception e) {
			System.out.println(e);
		}
		profileData.setIntervalDatas(intervalDatas);
	}

	//TODO clean up the reason for these methods
	private int parseF64(byte[] bArray, int offset) throws IOException {
		return parseF64(bArray, offset, 4);
	}
	private int parseF64(byte[] bArray, int offset, int len) throws IOException {
		int val = ProtocolUtils.getInt(bArray, offset, len);
		return val;

	}

}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.mbus.core.discover;

import com.energyict.protocolimpl.mbus.core.ApplicationData;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.MBus;
import com.energyict.protocolimpl.mbus.core.connection.MBusException;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870Frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SecondaryAddressDiscover {
	final int DEBUG=1;

	boolean started=false;
	int[] id = new int[] { 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf, 0xf };
	int idIndex;

	int[] manuf = new int[] { 0xFF, 0xff };
	int manufIndex = 0; // positions 01

	int medium = 0xff;
	int version = 0xff;
	int manufValue = 0xFFFF;
	long idValue = 0xFFFFFFFFL;

	List serialIds = new ArrayList();
	List<CIField72h> cIField72hs = new ArrayList();
	MBus mBus;

	public SecondaryAddressDiscover(MBus mBus) {
		this.mBus=mBus;
	}

	public void discover() throws IOException {

		String serialId = null;

		while (true) {
			try {
				serialId = idSearch();
				if (serialId == null) {
					break;
				}
			} catch (MBusException eIdSearch) {
				while (true) {
					try {
						serialId = manufSearch();
						if (serialId == null) {
							manufValue = 0xFFFF;
							break;
						}
					} catch (MBusException eManufSearch) {
						while (true) {
							try {
								serialId = versionSearch();
								if (serialId == null) {
									version = 0xff;
									break;
								}
							} catch (MBusException eVersionSearch) {
								while (true) {
									try {
										serialId = mediumSearch();
										if (serialId == null) {
											medium = 0xff;
											break;
										}
									} catch (MBusException eMediumSearch) {
										break;

									} // catch(IOException eMediumSearch)
								} // while(true)
							} // catch(IOException eVersionSearch)
						} // while(true)
					} // catch(IOException eManufSearch)
				} // while(true)
			} // catch(IOException eIdSearch)
		} // while(true)
	} // public void discover() throws IOException

	public String mediumSearch() throws IOException {
		while (true) {
			if (medium == 0xff)
				medium = 0;
			else if (medium < 0xFE)
				medium++;
			else if (medium == 0xFE)
				return null;
			else
				throw new IOException("Fatal error! medium = " + medium
						+ " is impossible...");

			if (DEBUG>= 2) System.out.println(Long.toHexString(medium));

			try {
				String retVal = findSecondaryAddress();
				if (retVal != null) {
					if (DEBUG>= 1) System.out.println("medium " + retVal + " found...");
				} else
					if (DEBUG>= 2) System.out.println("No medium found...");
			} catch (MBusException e) {
				if (DEBUG>= 2) System.out.println("No more value's... we need to use additional fields...");
				throw e;
			}
		}

	} // mediumSearch()

	public String versionSearch() throws IOException {

		while (true) {

			if (version == 0xff)
				version = 0;
			else if (version < 0xFE)
				version++;
			else if (version == 0xFE)
				return null;
			else
				throw new IOException("Fatal error! version = " + version
						+ " is impossible...");

			if (DEBUG>= 2) System.out.println(Long.toHexString(version));

			try {
				String retVal = findSecondaryAddress();
				if (retVal != null) {
					if (DEBUG>= 1) System.out.println("version " + retVal + " found...");
				} else
					if (DEBUG>= 2) System.out.println("No version found...");
			} catch (MBusException e) {
				if (DEBUG>= 2) System.out
						.println("No more value's... we need to use additional fields...");
				throw e;
			}
		}

	} // versionSearch()

	public String manufSearch() throws IOException {

		while (true) {
			if ((manufIndex == 0) && (manuf[manufIndex] == 254)) {
				return null;
			} else if ((manufIndex == 0) && (manuf[manufIndex] < 255)) {
				manuf[manufIndex]++;
			} else if ((manufIndex == 1) && (manuf[manufIndex] < 254)) {
				manuf[manufIndex]++;
			} else if ((manufIndex == 0) && (manuf[manufIndex] == 255)) {
				manuf[manufIndex] = 0;
			} else if ((manufIndex == 1) && (manuf[manufIndex] == 254)) {
				if (manufIndex > 0) {
					manuf[manufIndex] = 0xFF;
					manufIndex--;
					continue;
				} else
					return null;
			} else
				throw new IOException("Fatal error! Value of manuf["
						+ manufIndex + "] = " + manuf[manufIndex]
						+ " is impossible...");

			manufValue = (manuf[0] << 8) | manuf[1];
			if (DEBUG>= 2) System.out.println(Long.toHexString(manufValue));

			try {

				String retVal = findSecondaryAddress();

				if (retVal != null) {
					if (DEBUG>= 1) System.out.println("manufacturer " + retVal + " found...");
				} else
					if (DEBUG>= 2) System.out.println("No manufacturer found...");

			} catch (MBusException e) {

				if (manufIndex == 1) {
					if (DEBUG>= 2) System.out
							.println("No more manufacturers's... we need to use additional fields...");
					throw e;
				} else {
					if (DEBUG>=2) System.out.println(e.toString());
					manufIndex++;
					manuf[manufIndex] = -1;
				}
			}
		}
	} // manufSearch()

	public String idSearch() throws IOException {

		while (true) {

			if (id[idIndex] < 9) {
				id[idIndex]++;
			} else if (id[idIndex] == 9) {
				if (idIndex > 0) {
					id[idIndex] = 0xF;
					idIndex--;
					continue;
				} else
					return null;
			} else if (id[idIndex] == 0xF) {
				id[idIndex] = 0;
			} else
				throw new IOException("Fatal error! Value of id[" + idIndex
						+ "] = " + id[idIndex] + " is impossible...");

			idValue = buildValue(id);
			if (DEBUG>= 2) System.out.println(Long.toHexString(idValue));

			try {
				String retVal = findSecondaryAddress();
				if (retVal != null) {
					if (DEBUG>= 1) System.out.println("Address " + retVal + " found...");
				} else
					if (DEBUG>= 2) System.out.println("No address found...");

			} catch (MBusException e) {
				if (DEBUG>=2) System.out.println(e.toString());
				if (idIndex == 7) {
					if (DEBUG>= 2) System.out
							.println("No more id's... we need to use additional fields...");
					throw e;
				} else {
					idIndex++;
				}
			}

		} // while(true)

	} // idSearch()

	private long buildValue(int[] value) {
		long valueVal = 0L;
		for (int i = 0; i < value.length; i++)
			valueVal |= (value[i] << (((value.length - 1) - i) * 4));
		return valueVal & 0xFFFFFFFFL;
	}

    private CIField72h getCIField72h() throws IOException {
    	ApplicationData frame = mBus.getMBusConnection().sendREQ_UD2(true).getASDU();
    	if (frame == null) {
			try {
				Thread.sleep(2500);
			}
			catch(InterruptedException ex) {
				// absorb
			}
    		throw new MBusException("MBus, Framing error!");
    	}
    	CIField72h cIField72h = (CIField72h)frame.buildAbstractCIFieldObject(mBus.getTimeZone());
        return cIField72h;
    }

    private String findSecondaryAddress() throws IOException {
    	String serialId = doFindSecondaryAddress();
    	if (serialId != null)
    		serialIds.add(serialId);
    	return serialId;
    }
	private String doFindSecondaryAddress() throws IOException {

		String serid = Long.toHexString(idValue) + "_"
				+ Integer.toHexString(manufValue) + "_"
				+ Integer.toHexString(version) + "_"
				+ Integer.toHexString(medium);
		if (DEBUG>=1) System.out.println("try selecting " + serid);

		if (mBus != null) {
			while(true) {
		    	try {
			    	IEC870Frame frame =  mBus.getMBusConnection().selectSecondaryAddress(idValue,manufValue,version,medium,true);
			    	started = true;
			    	try {
						CIField72h o = getCIField72h();


						boolean found=false;
						for (int i=0;i<cIField72hs.size();i++) {
							CIField72h comp = (CIField72h)cIField72hs.get(i);
							if (comp.getDeviceSerialNumberSecundaryAddress().compareTo(o.getDeviceSerialNumberSecundaryAddress())==0) {
								found=true;
								break;
							}
						}
						if (!found)
							cIField72hs.add(o);

						return o.header();

			    	}
			    	catch(IEC870ConnectionException e) {
						if (DEBUG>=2) System.out.println("getCIField72h() "+e.toString());
						try {
							Thread.sleep(2500);
						}
						catch(InterruptedException ex) {
							// absorb
						}
			    		if (e.getReason() != mBus.getMBusConnection().getReasonTIMEOUT_ERROR()) {
			    			throw new MBusException(e.toString());
			    		}
			    	}
			    	catch(IOException e) {
			    		throw e;
			    	}
//			    	finally {
//						try {
//							mBus.getMBusConnection().sendSND_NKE();
//							try {
//								Thread.sleep(500);
//							}
//							catch(InterruptedException ex) {
//								// absorb
//							}
//						}
//						catch(IOException e) {
//							// absorb
//						}
//			    	}
		    	}
		    	catch(IEC870ConnectionException e) {
					if (DEBUG>=2) System.out.println("selectSecondaryAddress(...) "+e.toString());

		    		if (e.getReason() != mBus.getMBusConnection().getReasonTIMEOUT_ERROR()) {
						try {
							Thread.sleep(2500);
						}
						catch(InterruptedException ex) {
							// absorb
						}
		    			throw new MBusException(e.toString());
		    		}
		    		else {
		    			if (!started) {
		    				started = true;
		    				continue;
		    			}
		    		}

		    	}

		    	break;
			} // while(true)

		}
		else {
			// if (idValue == 0x1FFFFFFF)
			// return
			// "12345678_"+Integer.toHexString(manufValue)+"_"+Integer.toHexString(version)+"_"+Integer.toHexString(medium);
			if ((manufValue == 0xffff) && (version == 0xFF) && (medium == 0xFF)) {
				if (idValue == 0x2FFFFFFF)
					throw new MBusException("collision!");
				if (idValue == 0x23FFFFFF)
					throw new MBusException("collision!");
				if (idValue == 0x234FFFFF)
					throw new MBusException("collision!");
				if (idValue == 0x2344FFFF)
					throw new MBusException("collision!");
				if (idValue == 0x23445FFF)
					throw new MBusException("collision!");
				if (idValue == 0x234456FF)
					throw new MBusException("collision!");
				if (idValue == 0x2344567F)
					throw new MBusException("collision!");
				if (idValue == 0x23445678)
					throw new MBusException("collision!");

				if (idValue == 0x6FFFFFFF)
					throw new MBusException("collision!");
				if (idValue == 0x65FFFFFF)
					return "65121234_1345_aa_4";


				// if (idValue == 0x21FFFFFF)
				// return
				// "21345678_"+Integer.toHexString(manufValue)+"_"+Integer.toHexString(version)+"_"+Integer.toHexString(medium);
				// if (idValue == 0x24FFFFFF)
				// return
				// "24345678_"+Integer.toHexString(manufValue)+"_"+Integer.toHexString(version)+"_"+Integer.toHexString(medium);
			} else if ((version == 0xFF) && (medium == 0xFF)) {

				if (manufValue == 0x12FF)
					return "21345678_"+Integer.toHexString(manufValue)+"_" + Integer.toHexString(version) + "_"+ Integer.toHexString(medium);

				if (manufValue == 0x13FF)
					throw new MBusException("collision!");
				if (manufValue == 0x1345)
					throw new MBusException("collision!");
			}
			else if (medium == 0xFF) {

				if (version == 0x55)
					return "21345678_1345_55_"+ Integer.toHexString(medium);
				if (version == 0xAA)
					throw new MBusException("collision!");
			}
			else {
				if (medium == 0x04)
					return "21345678_1345_aa_4";
			}


		}

		return null;
	}

	public List<CIField72h> getCIField72hs() {
		return cIField72hs;
	}

}

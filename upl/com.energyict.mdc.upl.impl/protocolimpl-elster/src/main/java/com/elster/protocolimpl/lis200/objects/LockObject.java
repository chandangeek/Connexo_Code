package com.elster.protocolimpl.lis200.objects;

import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.commands.WriteCommand;

import java.io.IOException;

/**
 *
 * @author heuckeg
 *
 * This class is a class for lis200 locks
 *
 */
public class LockObject extends AbstractObject {

	public enum STATE {undefined, closed, open, opened}
	
	public static int MAXLOCKS = 5;
	
	public static int MANUFACTURERLOCK = 0;
	public static int SUPPLIERLOCK = 1;
	public static int CUSTOMERLOCK = 2;
	public static int MAINTENANCELOCK = 3;
	public static int NETOPERATORLOCK = 4;
	
	public static String MANUFACTURELOCK_NAME = "ManufacturerLock";
	public static String SUPPLIERLOCK_NAME = "SupplierLock";
	public static String CUSTOMERLOCK_NAME = "CustomerLock";
	public static String MAINTENANCELOCK_NAME = "MaintenanceLock";
	public static String NETOPERATORLOCK_NAME = "NetoperatorLock";

	public static int MANUFACTURERLOCK_INSTANCE = 2;
	public static int SUPPLIERLOCK_INSTANCE = 3;
	public static int CUSTOMERLOCK_INSTANCE = 4;
	public static int MAINTENANCELOCK_INSTANCE = 5;
	public static int NETOPERATORLOCK_INSTANCE = 6;
	
	public static String[] LockNames = {MANUFACTURELOCK_NAME,
										SUPPLIERLOCK_NAME,
										CUSTOMERLOCK_NAME,
										MAINTENANCELOCK_NAME,
 										NETOPERATORLOCK_NAME};
	
	public static int[] LockNo = {      MANUFACTURERLOCK,
										SUPPLIERLOCK,
										CUSTOMERLOCK,
										MAINTENANCELOCK,
										NETOPERATORLOCK};
		
	public static int[] LockInstance = {MANUFACTURERLOCK_INSTANCE,
										SUPPLIERLOCK_INSTANCE,
										CUSTOMERLOCK_INSTANCE,
										MAINTENANCELOCK_INSTANCE,
										NETOPERATORLOCK_INSTANCE};
	
	/** The startAddress of this object */
	private static String keyAddress = "0171.0";
	private static String lockAddress = "0170.0";

	/** The instance of the object */
	private int instance = 0;

	private String name = "unkown";
	
	/**
	 * @param link
	 * @param instance is instance of lock object (1..3)
	 */
	public LockObject(ProtocolLink link, int instance) {
		super(link);
		this.instance = instance;
		
		for(int i = 0; i < LockInstance.length; i++) {
			if (LockInstance[i] == instance) {
				name = LockNames[i];
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getInitialAddress() {
		return  lockAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int getObjectInstance() {
		return instance;
	}

	/**
	 * checks, if the lock is open
	 *
	 * @return true if lock is open
	 * @throws java.io.IOException
	 */
	public boolean isLockOpen() throws IOException {
	    String val = getValue();
		return val.equals("1");
	}

    /**
     * Builds a complete address in the format <instance>:<address>
     *
     * @param address
     * @return
     */
	protected String getStartAddress(String address) {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(constructInstanceString());
		strBuilder.append(COLON);
		strBuilder.append(address);
		return strBuilder.toString();
	}

    /**
     * This function tries to open the lock with the given key
     * Is lock is opened correctly has to checked with isLockOpen (after openLock)
     *
     * @param key
     * @throws java.io.IOException
     */
	public void openLock(String key) throws IOException {
		WriteCommand wc = new WriteCommand(link);

		wc.setStartAddress(getStartAddress(keyAddress));
		byte[] setValue = key.getBytes();
		wc.setDataValue(setValue);
		wc.invoke();
	}

	/**
	 * Closes the lock
	 *
	 * @throws java.io.IOException
	 */
	public void closeLock() throws IOException {
		this.setValue(new byte[] { 0x30 });
	}

	/**
	 * Changes the key for an open lock
	 *
	 * @param key
	 * @throws java.io.IOException
	 */
	public void changeKey(String key) throws IOException {
		WriteCommand wc = new WriteCommand(link);
		wc.setStartAddress(getStartAddress(keyAddress));
		byte[] setValue = key.getBytes();
		wc.setDataValue(setValue);
		wc.invoke();
	}
	
	/** 
	 * Getter for name of lock
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}
}

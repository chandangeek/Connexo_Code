package com.elster.protocolimpl.lis200.objects;

import com.elster.protocolimpl.lis200.commands.LisDeviceError;
import com.elster.protocolimpl.lis200.commands.ReadCommand;
import com.elster.protocolimpl.lis200.commands.WriteCommand;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;

/**
 *
 * @author heuckeg
 *
 * This class is a class for lis200 locks
 *
 */
@SuppressWarnings("unused")
public enum LockObject
{
    // common LIS200 locks
    ManufacturerLock(2, "ManufacturerLock"),
    SupplierLock(3, "SupplierLock"),
    CustomerLock(4, "CustomerLock"),

    // EK280 locks
    AdministratorLock(3, "AdministratorLock"),
    DataCollectorLock(5, "DataCollectorLock"),
    UserLock6(6, "UserLock6");

	public enum STATE {undefined, closed, open, opened}

	/** The startAddress of this object */
	protected static String keyAddress = "0171.0";
	protected static String lockAddress = "0170.0";

	/** The instance of the object */
	private final int instance;
	private final String name;
    private STATE state;
    private ProtocolLink link = null;

	LockObject(final int instance, final String name) {
		this.instance = instance;
        this.name = name;
        state = STATE.undefined;
	}

    public String getName()
    {
        return name;
			}

    public STATE getState()
    {
        return state;
		}

    public void setLink(final ProtocolLink link)
    {
        this.link = link;
	}

	public boolean isLockOpen() throws IOException
    {
		return getLockState() == STATE.open;
	}

    public STATE getLockState() throws IOException
    {
        final String lockAddress = instance + ":" + LockObject.lockAddress;
        ReadCommand rc = new ReadCommand(link);
        rc.setStartAddress(lockAddress);
        try
        {
            String result = rc.invoke();
            if ((result == null) || (result.length() == 0))
            {
                throw new IOException("getLockState(" + lockAddress + "): empty read!");
	}
            link.getLogger().info("---- getLockState(" + lockAddress + "): received " + result);
            String value = ProtocolUtils.stripBrackets(result);
            return value.equals("1") ? STATE.open : STATE.closed;
        }
        catch (LisDeviceError e)
        {
            link.getLogger().info("---- getLockState(" + lockAddress + "): LisDeviceError(" + e.getMessage() + ")");
            return STATE.closed;
        }
	}

    /**
     * This function tries to open the lock with the given key
     * Is lock is opened correctly has to checked with isLockOpen (after openLock)
     *
     * @param key - to open lock with
     * @throws IOException in case of an error
     */
	public STATE openLock(String key) throws IOException
    {
        final String keyAddress = instance + ":" + LockObject.keyAddress;
		WriteCommand wc = new WriteCommand(link);
		wc.setStartAddress(keyAddress);
        wc.setDataValue(key.getBytes());
		wc.invoke();

        link.getLogger().info("---- openLock(" + keyAddress + ") with " + key);
        STATE state = getLockState();
        return state == STATE.open ? STATE.opened : state;
	}

	/**
	 * Closes the lock
	 *
	 * @throws IOException
	 */
	public void closeLock() throws IOException {
        final String keyAddress = instance + ":" + lockAddress;
		WriteCommand wc = new WriteCommand(link);
        wc.setStartAddress(keyAddress);
        wc.setDataValue("0".getBytes());
		wc.invoke();
	}
}

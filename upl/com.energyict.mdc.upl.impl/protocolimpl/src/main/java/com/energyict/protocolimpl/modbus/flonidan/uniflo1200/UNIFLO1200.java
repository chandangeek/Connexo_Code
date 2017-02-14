/*
 * UNIFLO1200.java
 *
 * Created on 4-dec-2008, 15:00:50 by jme
 *
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.connection.UNIFLO1200Connection;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers.UNIFLO1200Parsers;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.UNIFLO1200Profile;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200RegisterFactory;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;

/**
 * @author jme
 *
 */
public class UNIFLO1200 extends Modbus implements SerialNumberSupport {

	private static final int DEBUG = 0;

	private static final int MIN_LOADPROFILE_NUMBER = 1;
	private static final int MAX_LOADPROFILE_NUMBER = 3;

	private int secLvl = 0;
	private UNIFLO1200Profile loadProfile;
	private int loadProfileNumber;

	public UNIFLO1200(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeout, int retries, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        modbusConnection = new UNIFLO1200Connection(inputStream, outputStream, timeout, getInterframeTimeout(), retries, forcedDelay, echoCancelling, halfDuplexController, getLogger());
        return getModbusConnection();
    }

    @Override
    public String getSerialNumber() {
        try {
            return (String) getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_SERIAL_NUMBER).value();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

	@Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:28 +0200 (Thu, 26 Nov 2015)$";
    }

	public int getLoadProfileNumber() {
		return loadProfileNumber;
	}

	public void setLoadProfileNumber(int loadProfileNumber) {
		this.loadProfileNumber = loadProfileNumber;
	}

	@Override
	public void setTime() throws IOException {
		byte[] b;
		Calendar cal = ProtocolUtils.getCleanCalendar(gettimeZone());
		cal.setTime(new Date());
		b = UNIFLO1200Parsers.buildTimeDate(cal);

		try {
			getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_TIME).getWriteMultipleRegisters(b);
		} catch (IOException e) {
			throw new ProtocolException("Unable to set time. Possibly wrong password. Exception message: " + e.getMessage());
		}

	}

	@Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		return getLoadProfile().getProfileData(from, to, includeEvents);
	}

	@Override
	public int getProfileInterval() throws IOException {
		sendDebug("getProfileInterval()", 5);
		return getLoadProfile().getProfileInterval();
	}

	@Override
	public int getNumberOfChannels() throws IOException {
		return getLoadProfile().getNumberOfChannels();
	}

	@Override
	public Date getTime() throws IOException {
		return getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_TIME).dateValue();
	}

	@Override
	public String getFirmwareVersion() throws IOException {
		return (String) getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_DEVICE_TYPE).value();
	}

	@Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		return ((UNIFLO1200RegisterFactory)getRegisterFactory()).readRegister(obisCode);
	}

	@Override
	protected void doTheConnect() throws IOException {
		sendDebug("doTheConnect()", 5);
		String password = getInfoTypePassword();
		sendDebug(password, 5);

		if (password != null) {
			byte[] b = password.getBytes();
			getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_LOGIN).getWriteMultipleRegisters(b);
		}

		setSecLvl(((Integer)getRegisterFactory().findRegister(UNIFLO1200RegisterFactory.REG_ACTUAL_SECLEVEL).value()).intValue());

		if (getInfoTypeSecurityLevel() != getSecLvl()) {
            throw new InvalidPropertyException("SecurityLevel mismatch [" + getInfoTypeSecurityLevel() + " != " + getSecLvl() + "]: Reason may be wrong password or hardware lock.");
        }

		sendDebug("Actual security lvl: " + getSecLvl(), 2);
		setLoadProfile(new UNIFLO1200Profile(this));

	}

	@Override
	protected void doTheDisConnect() throws IOException {
		sendDebug("doTheDisConnect()", 5);
	}

	@Override
	public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs =
				new ArrayList<>(super.getUPLPropertySpecs()
                        .stream()
                        .filter(propertySpec -> !propertySpec.getName().equals(PASSWORD.getName()))
                        .collect(Collectors.toList()));
        PropertySpecService propertySpecService = this.getPropertySpecService();
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder(PASSWORD.getName(), false, () -> propertySpecService.stringSpecOfMaximumLength(8))
                        .finish());
        propertySpecs.add(
                UPLPropertySpecFactory
                        .specBuilder("LoadProfileNumber", false, propertySpecService::integerSpec)
                        .addValues(MIN_LOADPROFILE_NUMBER, MAX_LOADPROFILE_NUMBER)
                        .finish());
        return propertySpecs;
	}

	@Override
	public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
		super.setUPLProperties(properties);
		sendDebug("setProperties()", 5);
        try {
            setLoadProfileNumber(Integer.parseInt(properties.getTypedProperty("LoadProfileNumber", "1").trim()));
            if (getInfoTypePassword() != null) {
                while (getInfoTypePassword().length() < 8) {
                    setInfoTypePassword(getInfoTypePassword() + " ");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

	@Override
	protected void initRegisterFactory() {
		sendDebug("initRegisterFactory()", 5);
        setRegisterFactory(new UNIFLO1200RegisterFactory(this));
	}

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
    	StringBuilder builder = new StringBuilder();
    	if (extendedLogging==1) {
            for (AbstractRegister ar : ((UNIFLO1200RegisterFactory) getRegisterFactory()).getRegisters()) {
                if (ar.getObisCode() != null) {
                    builder.append(ar.getObisCode().toString()).append(", ");
                    builder.append("unit = ").append(ar.getUnit()).append(", ");
                    builder.append("name = ").append(ar.getName());
                    builder.append("\n");
                }
            }
    	}
    	return builder.toString();
    }

	private int getSecLvl() {
		return secLvl;
	}

	private void setSecLvl(int secLvl) {
		this.secLvl = secLvl;
	}

    public UNIFLO1200Profile getLoadProfile() {
		return loadProfile;
	}

	public void setLoadProfile(UNIFLO1200Profile loadProfile) {
		this.loadProfile = loadProfile;
	}

	public static void main(String[] args) {
		try {

			UNIFLO1200Parsers uflp = new UNIFLO1200Parsers(null);

			int[] values = {1,0};
			UNIFLO1200Parsers.REAL32Parser parser = uflp.new REAL32Parser();
			System.out.println("Result: " + parser.val(values, null));


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void sendDebug(String message, int debuglvl) {
		if (DEBUG == 0) {
	    	message = " [" + new Date().toString() + "] > " + message;
		} else {
	    	message = " ##### DEBUG [" + new Date().toString() + "] ######## > " + message;
		}
    	if ((debuglvl <= DEBUG) && (getLogger() != null)) {
    		getLogger().info(message);
    		System.out.println(message);
    	}
    }

}
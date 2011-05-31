package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.base.AbstractProtocolProperties;
import com.energyict.protocolimpl.base.ProtocolProperty;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 1/03/11
 * Time: 14:59
 */
public class ValidationProperties extends AbstractProtocolProperties {

    private static final String GROUP_EXTERNAL_NAME = "GroupExternalName";
    private static final String CHANNELS_TO_VALIDATE = "ChannelsToValidate";
    private static final String PARENT_FOLDER_EXT_NAME = "ParenFolderExtName";
    private static final String VALIDATE_INSTALLATION_DATE = "ValidateInstallationDate";
    private static final String VALIDATE_MISSING_DATA = "ValidateMissingData";
    private static final String VALIDATE_WRONG_DATA = "ValidateWrongData";
    private static final String WRITE_TO_FILE = "WriteToFile";
    private static final String LIST_BEFORE = "ListDevicesInstalledBefore";
    private static final String LIST_AFTER = "ListDevicesInstalledAfter";
    private static final String INSTALLATION_DATE_FROM = "UseInstallationDateFrom";

    private static final String DEFAULT_GROUP_EXTERNAL_NAME = null;
    private static final String DEFAULT_CHANNELS_TO_VALIDATE = "1,2,3,4,5,6,7,8,9,10,11";
    private static final String DEFAULT_PARENT_FOLDER_EXT_NAME = "meteringPoints";
    private static final String DEFAULT_VALIDATE_INSTALLATION_DATE = "0";
    private static final String DEFAULT_VALIDATE_MISSING_DATA = "0";
    private static final String DEFAULT_VALIDATE_WRONG_DATA = "0";
    private static final String DEFAULT_WRITE_TO_FILE = "0";
    private static final String DEFAULT_LIST_BEFORE = null;
    private static final String DEFAULT_LIST_AFTER = null;
    private static final String DEFAULT_INSTALLATION_DATE_FROM = null;

    public ValidationProperties(String file) {
        super(new Properties());
        try {
            FileInputStream fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            addProperties(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doValidateProperties() throws MissingPropertyException, InvalidPropertyException {

    }

    public List<String> getOptionalKeys() {
        return new ArrayList<String>();
    }

    public List<String> getRequiredKeys() {
        return new ArrayList<String>();
    }

    public String getGroupExternalName() {
        return getStringValue(GROUP_EXTERNAL_NAME, DEFAULT_GROUP_EXTERNAL_NAME);
    }

    @ProtocolProperty
    public List<Integer> getChannelsToValidate() {
        String[] strings = getChannelsToValidateAsString().split(",");
        Integer[] ints = new Integer[strings.length];
        for (int i = 0; i < ints.length; i++) {
            try {
                ints[i] = Integer.valueOf(strings[i]);
            } catch (NumberFormatException e) {
                ints[i] = -1;
            }
        }
        return Arrays.asList(ints);
    }

    @ProtocolProperty
    public String getChannelsToValidateAsString() {
        return getStringValue(CHANNELS_TO_VALIDATE, DEFAULT_CHANNELS_TO_VALIDATE);
    }

    @ProtocolProperty
    public String getParentFolderExtName() {
        return getStringValue(PARENT_FOLDER_EXT_NAME, DEFAULT_PARENT_FOLDER_EXT_NAME);
    }

    @ProtocolProperty
    public boolean isValidateInstallationDate() {
        return getIntProperty(VALIDATE_INSTALLATION_DATE, DEFAULT_VALIDATE_INSTALLATION_DATE) == 1;
    }

    @ProtocolProperty
    public boolean isValidateMissingData() {
        return getIntProperty(VALIDATE_MISSING_DATA, DEFAULT_VALIDATE_MISSING_DATA) == 1;
    }

    @ProtocolProperty
    public boolean isValidateWrongData() {
        return getIntProperty(VALIDATE_WRONG_DATA, DEFAULT_VALIDATE_WRONG_DATA) == 1;
    }

    @ProtocolProperty
    public boolean isWriteToFile() {
        return getIntProperty(WRITE_TO_FILE, DEFAULT_WRITE_TO_FILE) == 1;
    }

    @ProtocolProperty
    public String getListDevicesInstalledBeforeString() {
        return getStringValue(LIST_BEFORE, DEFAULT_LIST_BEFORE);
    }

    @ProtocolProperty
    public String getListDevicesInstalledAfterString() {
        return getStringValue(LIST_AFTER, DEFAULT_LIST_AFTER);
    }

    @ProtocolProperty
    public String getInstallationDateFromString() {
        return getStringValue(INSTALLATION_DATE_FROM, DEFAULT_INSTALLATION_DATE_FROM);
    }

    @ProtocolProperty
    public Date getOnlyFrom() {
        if (getInstallationDateFromString() == null) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(ValidationUtils.DATE_FORMAT);
            return format.parse(getInstallationDateFromString());
        } catch (ParseException e) {
            ValidationUtils.log(Level.SEVERE, "Unable to parse [" + getInstallationDateFromString() + "] as date. Should have this format [" + ValidationUtils.DATE_FORMAT + "]");
            return null;
        }
    }

    @ProtocolProperty
    public Date getListDevicesInstalledBeforeDate() {
        if (getListDevicesInstalledBeforeString() == null) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(ValidationUtils.DATE_FORMAT);
            return format.parse(getListDevicesInstalledBeforeString());
        } catch (ParseException e) {
            ValidationUtils.log(Level.SEVERE, "Unable to parse [" + getListDevicesInstalledBeforeString() + "] as date. Should have this format [" + ValidationUtils.DATE_FORMAT + "]");
            return null;
        }
    }

    @ProtocolProperty
    public Date getListDevicesInstalledAfterDate() {
        if (getListDevicesInstalledAfterString() == null) {
            return null;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(ValidationUtils.DATE_FORMAT);
            return format.parse(getListDevicesInstalledAfterString());
        } catch (ParseException e) {
            ValidationUtils.log(Level.SEVERE, "Unable to parse [" + getListDevicesInstalledAfterString() + "] as date. Should have this format [" + ValidationUtils.DATE_FORMAT + "]");
            return null;
        }
    }
}

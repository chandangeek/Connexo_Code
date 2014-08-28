package com.energyict.mdc.common.license;

import com.energyict.mdc.common.BusinessException;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a license.
 */
public interface License extends Serializable {

    static final int CURRENT_lICENSEFORMAT = 3;   // Since 9.1

    // **************************
    // Keys for the modules
    // **************************
    static final String MODULE_mSETTLE = "mSettle";
    static final String MODULE_mPREBILLINVOICE = "mPreBillInvoice";
    static final String MODULE_mPREBILLCOSTALLOCATION = "mPreBillCostAllocation";
    static final String MODULE_mAUTOMATE = "mAutomate";
    static final String MODULE_mALARM = "mAlarm";
    static final String MODULE_mEXCHANGE = "mExchange";
    static final String MODULE_mVALIDATE = "mValidate";
    static final String MODULE_hxMobile = "hxMobile";
    static final String MODULE_mCOLLECT = "mCollect";
    static final String MODULE_mDATAMODEL = "mDataModel";

    // **************************
    // Keys for the resource limits
    // **************************
    static final String DEVICELIMIT = "deviceLimit";
    static final String CHANNELLIMIT = "channelLimit";
    static final String REGISTERLIMIT = "registerLimit";
    static final String OPERATIONAL_USERLIMIT = "operationalUserLimit";
    static final String CUSTOMER_ENGAGEMENT_USERLIMIT = "customerEngagementUserLimit";
    static final String MOBILE_USERLIMIT = "mobileUserLimit";

    // **************************
    // Keys for information
    // **************************
    static final String FORMAT = "format";
    static final String LICENSOR = "licensor";
    static final String LICENSEE = "licensee";
    static final String DESCRIPTION = "description";
    static final String SYSTEMTYPE = "systemType";
    static final String COMMENT = "comment";
    static final String EXPIRES = "expires";
    static final String TOLERATE = "tolerate";
    static final String LICENSE_REQUEST = "licenseRequest";
    static final String EVALUATIONVERSION = "evaluationVersion";

    // **************************
    // Keys for clients
    // **************************
    static final String EIMASTER = "eiMaster";
    static final String EIDESIGNER = "eiDesigner";
    static final String EIPORTAL = "eiPortal";

    // **************************
    // Keys for interfaces (need to start with 'ix')
    // **************************
    static final String INTERFACE_PREFIX = "ix";
    static final String IXMDUS = "ixMDUS";

    // **************************
    // Keys for headend (need to start with 'hx')
    // **************************
    static final String HEADEND_PREFIX = "hx";

    // **************************
    // Keys for validation/estimation rules
    // **************************
    static final String CHANNEL_VALIDATION_RULES = "channelValidationRules";
    static final String REGISTER_VALIDATION_RULES = "registerValidationRules";
    static final String CHANNEL_ESTIMATION_RULES = "channelEstimationRules";

    // Key for licensed protocols
    static final String PROTOCOL_FAMILIES = "protocolFamilies";
    static final String PROTOCOLS = "protocols";

    //Key for allowing all protocols
    static final String ALL = "all";

    /**
     * Returns the license properties
     *
     * @return the license properties
     */
    Properties getProperties();

    /**
     * Returns the property with the given name or null
     *
     * @param key property key to match
     * @return the property value or null
     */
    String getProperty(String key);

    /**
     * Returns the given property
     *
     * @param key          property key to match
     * @param defaultValue if the property does not exists
     * @return the property value or the defaultValue
     */
    String getProperty(String key, String defaultValue);

    boolean isCurrentFormat();

    /**
     * indicates whether the license includes the mPreBillCostAllocation component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mPreBillCostAllocation();

    /**
     * indicates whether the license includes the mCollect component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mCollect();

    /**
     * indicates whether the license includes the mSettle component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mSettle();

    /**
     * indicates whether the license includes the mExchange module
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mExchange();

    /**
     * indicates whether the license includes the hxMobile component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_hxMobile();

    /**
     * indicates whether the license includes the mDataModel component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mDataModel();

    /**
     * Returns the number of licensed rtu's
     * -1 means unlimited
     * 0 means none
     *
     * @return the number of licensed rtu's .
     */
    int getDeviceLimit();

    /**
     * Returns the date the calculation of the different limits (Device/Channel/Register) happened
     *
     * @return the date the calculation of the different limits happened
     */
    Date getLastCalculationDate();

    /**
     * Returns the date the calculation detected a license violation
     *
     * @return the date the calculation detected a license violation
     */
    Date getViolationDate();

    /**
     * Returns the date the license needs to be recovered after license violation
     *
     * @return the date the license needs to be recovered after license violation
     */
    Date getLastConvalescenceDate();

    /**
     * Returns the current number of active rtu's
     *
     * @return the number of active rtu's.
     */
    int getCurrentDeviceCount();

    /**
     * Returns the number of licensed channels
     * -1 means unlimited
     * 0 means none
     *
     * @return the number of licensed channels.
     */
    int getChannelLimit();

    /**
     * Returns the current number of active channels
     *
     * @return the number of active channels
     */
    int getCurrentChannelCount();

    /**
     * Returns the number of licensed <Code>RtuRegisters</Code>
     *
     * @return the number of licensed RtuRegisters.
     */
    int getRegisterLimit();

    /**
     * Returns the current number of active <Code>RtuRegisters</Code>
     *
     * @return the number of active <Code>RtuRegisters</Code>
     */
    int getCurrentRegisterCount();

    /**
     * Returns the number of licensed operational users (aka 'desktop users')
     * -1 means unlimited
     * 0 means none
     *
     * @return the number of licensed operational users.
     */
    int getOperationalUserLimit();

    /**
     * Returns the current number of defined full users
     *
     * @return the full user count
     */
    int getCurrentOperationalUserCount();

    /**
     * Returns the number of licensed customer engagement users (previously known as 'web users')
     * -1 means unlimited
     * 0 means none
     *
     * @return the number of licensed customer engagement users.
     */
    int getCustomerEngagementUserLimit();

    /**
     * Returns the current number of defined customer engagement users
     *
     * @return the customer engagement user count
     */
    int getCurrentCustomerEngagementUserCount();

    /**
     * Returns the number of licensed mobile users
     * -1 means unlimited
     * 0 means none
     *
     * @return the number of licensed mobile users.
     */
    int getMobileUserLimit();

    /**
     * Returns the current number of mobile users
     *
     * @return the mobile user count (currently counted as the number of remote com servers)
     */
    int getCurrentMobileUserCount();

    /**
     * Returns the licensor's name
     *
     * @return the licensor
     */
    String getLicensor();

    /**
     * Returns the licensee's name
     *
     * @return the licensee
     */
    String getLicensee();

    /**
     * Returns the license expiration date
     *
     * @return the expiration date or null
     */
    Date getExpirationDate();

    /**
     * Returns the license request date
     *
     * @return the request date or null
     */
    Date getRequestDate();

    /**
     * Returns the license description
     *
     * @return the description
     */
    String getDescription();

    /**
     * Returns the license comment
     *
     * @return the comment
     */
    String getComment();

    /**
     * Returns the system type
     *
     * @return the system type
     */
    String getSystemType();

    /**
     * Internal use only
     *
     * @return true or false
     */
    boolean tolerate();

    /**
     * Test if the given protocol is included in the license
     *
     * @param name name of the protocol to test
     * @return true if licensed , false otherwise
     */
    boolean hasProtocol(String name);

    /**
     * Test if all protocols have been licensed
     *
     * @return true if licensed , false otherwise
     */
    boolean hasAllProtocols();

    /**
     * Test if the given channel validation rule is included in the license
     *
     * @param rule the channel validation rule
     * @return true if licensed , false otherwise
     */
    public boolean hasChannelValidationRule(LicensedChannelValidationRule rule);

    /**
     * Test if all channel validation rules are licensed
     *
     * @return true if licensed , false otherwise
     */
    public boolean hasAllChannelValidationRules();

    /**
     * Test if the given register validation rule is included in the license
     *
     * @param rule the register validation rule
     * @return true if licensed , false otherwise
     */
    public boolean hasRegisterValidationRule(LicensedRegisterValidationRule rule);

    /**
     * Test if all register validation rules are licensed
     *
     * @return true if licensed, false otherwise
     */
    public boolean hasAllRegisterValidationRules();

    /**
     * Test if the given channel estimation rule is included in the license
     *
     * @param rule the channel estimation rule
     * @return true if licensed, false otherwise
     */
    public boolean hasChannelEstimationRule(LicensedChannelEstimationRule rule);

    /**
     * Test if all channel estimation rules are licensed
     *
     * @return true if licensed, false otherwise
     */
    public boolean hasAllChannelEstimationRules();

    /**
     * Returns all licensed protocol families
     *
     * @return a Set of codes that uniquely identify each protocol family
     */
    Set<Integer> getProtocolFamilies();

    /**
     * Returns all licensed protocols
     *
     * @return a Set of codes that uniquely identify each protocol
     */
    Set<Integer> getProtocols();

    /**
     * Returns all licensed interfaces
     *
     * @return a Set of String objects
     */
    Set<String> getInterfaces();

    /**
     * Returns all licensed headEnd systems
     *
     * @return a Set of String objects
     */
    Set<String> getHeadEnds();

    /**
     * Test if the license is for evaluation only
     *
     * @return true if evaluation only license, false otherwise
     */
    boolean isEvaluationVersion();

    /**
     * indicates whether the license includes the mPreBillInvoice component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mPreBillInvoice();

    /**
     * indicates whether the license includes the mAutomate component
     * aka Business Process Management component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mAutomate();

    /**
     * indicates whether the license includes the mAlarm component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mAlarm();

    /**
     * indicates whether the license includes the mValidate component
     *
     * @return true if included false otherwise
     */
    boolean hasModule_mValidate();

    /**
     * Indicates whether the license includes usage of eiMaster
     *
     * @return true if usage of eiMaster is allowed, false otherwise
     */
    boolean hasEiMaster();

    /**
     * Indicates whether the license includes usage of eiDesigner
     *
     * @return true if usage of eiDesigner is allowed, false otherwise
     */
    boolean hasEiDesigner();

    /**
     * Indicates whether the license includes usage of eiPortal
     *
     * @return true if usage of eiPortal is allowed, false otherwise
     */
    boolean hasEiPortal();

    /**
     * Indicates whether the license includes usage of SAP Mdus connector
     *
     * @return true if allowed, false otherwise
     */
    boolean hasMdus();

    /**
     * @return a <Code>LicenseViolation</Code> object if a License violation has been detected, null if not
     */
    LicenseViolation getViolations();
}
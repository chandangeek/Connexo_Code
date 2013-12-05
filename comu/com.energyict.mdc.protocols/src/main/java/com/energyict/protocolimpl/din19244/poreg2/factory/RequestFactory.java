package com.energyict.protocolimpl.din19244.poreg2.factory;


import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.request.AlarmParameters;
import com.energyict.protocolimpl.din19244.poreg2.request.Firmware;
import com.energyict.protocolimpl.din19244.poreg2.request.ProfileData;
import com.energyict.protocolimpl.din19244.poreg2.request.ProfileDataEntry;
import com.energyict.protocolimpl.din19244.poreg2.request.SetTime;
import com.energyict.protocolimpl.din19244.poreg2.request.register.AlarmLinks;
import com.energyict.protocolimpl.din19244.poreg2.request.register.BillingParameters;
import com.energyict.protocolimpl.din19244.poreg2.request.register.ProfileDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Factory able to execute general requests, like reading the firmware version or setting the clock.
 *
 * Copyrights EnergyICT
 * Date: 20-apr-2011
 * Time: 14:09:45
 */
public class RequestFactory {

    Poreg poreg;

    public RequestFactory(Poreg poreg) {
        this.poreg = poreg;
    }

    //Add a new time alarm to the table. This one is used for to terminate the billing period.
    public void writeNewTimeAlarm(int alarmTimeIndex) throws IOException {
        AlarmParameters alarmParam = new AlarmParameters(poreg, alarmTimeIndex, 0, 1, 9);    //Only update the fields in row [alarmTimeIndex]
        alarmParam.setDate(new Date());
        alarmParam.write();
    }

    public void writeAlarmLink(int alarmTimeIndex) throws IOException {
        AlarmLinks link = new AlarmLinks(poreg, 0, 3 + alarmTimeIndex, 1, 1);
        link.setWriteValue(alarmTimeIndex);
        link.write();
    }

    public void initializeAlarmLink() throws IOException {
        AlarmLinks link = new AlarmLinks(poreg, 0, 0, 1, 1);
        link.setWriteValue(3);
        link.write();
    }

    public void setBillingAlarmIndex(int value) throws IOException {
        int numberOfBillingConfigs = poreg.getRegisterFactory().readBillingConfiguration().size();
        BillingParameters billingParameters = new BillingParameters(poreg, 0, 4, numberOfBillingConfigs, 1); //Only write the value to the fields in the 4th column
        billingParameters.setWriteValue(value);
        billingParameters.write();
    }

    public String readFirmwareVersion() throws IOException {
        Firmware firmware = new Firmware(poreg);
        firmware.doRequest();
        return firmware.getFirmware();
    }

    /**
     * When a ProtocolConnectionException (CRC error, corrupt frame,...) occurs, a retry mechanism is used.
     * This means, a new LP data request is sent, with a new from date, being the timestamp of the most recently received LP entry.
     */
    public List<ProfileDataEntry> readProfileData(int length, ProfileDescription description, int registerAddress, int fieldAddress, Date lastReading, Date toDate) throws IOException {
        List<ProfileDataEntry> profileDataEntries = new ArrayList<ProfileDataEntry>();

        ProfileData profileData;
        boolean isCorruptResponse = true;
        int count = 0;
        boolean firstBlockFirstAttempt = false;
        while (isCorruptResponse) {
            profileData = new ProfileData(length, description.getGid(), poreg, registerAddress, fieldAddress, 1, 1, description.getProfileId(), lastReading, toDate);
            profileData.doRequest();
            profileDataEntries.addAll(profileData.getProfileDataEntries());
            isCorruptResponse = profileData.isCorruptFrame();
            Date newLastReading = updateLastReading(profileDataEntries, lastReading);
            if (profileDataEntries.size() == 0 && newLastReading.equals(lastReading) && !firstBlockFirstAttempt && count == 0) {
                firstBlockFirstAttempt = true;       //Necessary to indicate this because a failed first block always has the same newLastReading as the original request
            } else {
                firstBlockFirstAttempt = false;
            }

            if (isCorruptResponse) {
                poreg.getLogger().warning("Received corrupted frame while requesting LP data");
                poreg.getLogger().warning("Cause: " + profileData.getCorruptCause());

                if (newLastReading.equals(lastReading) && !firstBlockFirstAttempt) {       //It's a retry
                    count++;     //Retry counter
                    if (count >= poreg.getConnection().getRetries()) {  //Stop retrying after X retries
                        String msg = "Still received a corrupt frame (" + "after " + poreg.getConnection().getRetries() + " retries) while trying to request LP data with fromDate = " + lastReading + ". Aborting.";
                        poreg.getLogger().severe(msg);
                        throw new IOException(msg);
                    }
                    poreg.getLogger().warning("Sending new request (retry " + count + "/" + poreg.getConnection().getRetries() + ") with fromDate = timestamp of the last received LP entry (" + newLastReading + ")");
                } else {                                        //It's a first attempt
                    count = 0;
                    poreg.getLogger().warning("Sending new profile data request for the remaining LP data. From date = timestamp of the last received LP entry (" + newLastReading + ")");
                }
            }
            lastReading = newLastReading;
        }
        return profileDataEntries;
    }

    private Date updateLastReading(List<ProfileDataEntry> profileDataEntries, Date lastReading) {
        if (profileDataEntries == null || profileDataEntries.size() == 0) {
            return lastReading;
        }
        int lastEntryIndex = profileDataEntries.size() - 1;
        return (Date) profileDataEntries.get(lastEntryIndex).getDate().clone();
    }

    public void setTime() throws IOException {
        SetTime setTime = new SetTime(poreg);
        Date time = getTimeStamp();
        setTime.setTime(time);
        setTime.write();
    }

    private Date getTimeStamp() {
        Calendar now = Calendar.getInstance(poreg.getTimeZone());
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        cal.set(Calendar.DATE, now.get(Calendar.DATE));
        cal.set(Calendar.DAY_OF_WEEK, now.get(Calendar.DAY_OF_WEEK));
        cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, now.get(Calendar.MILLISECOND));
        return cal.getTime();
    }

    public void resetDemand() throws IOException {
        setBillingPeriodTriggeredByAlarm();      //Enable billing resets by time alarms
        setBillingAlarmIndex(0);                 //0 = alarm index
        writeNewTimeAlarm(5);                    //Add new time alarm with current date, on row 5
        initializeAlarmLink();                   //Make sure billing registers are triggered by alarms
        writeAlarmLink(5);                       //Link the alarm index to the time alarm row
    }

    private void setBillingPeriodTriggeredByAlarm() throws IOException {
        int numberOfBillingConfigs = poreg.getRegisterFactory().readBillingConfiguration().size();
        if (numberOfBillingConfigs > 0) {
            BillingParameters billingParameters = new BillingParameters(poreg, 0, 3, numberOfBillingConfigs, 1); //Only write the value to the fields in the 4th column
            billingParameters.setWriteValue(4);
            billingParameters.write();
        } else {
            throw new IOException("No billing data is configured to be stored, unable to terminate the billing period");
        }
    }
}
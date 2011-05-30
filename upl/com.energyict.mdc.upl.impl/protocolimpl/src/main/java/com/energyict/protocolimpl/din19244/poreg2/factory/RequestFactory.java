package com.energyict.protocolimpl.din19244.poreg2.factory;


import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.request.*;
import com.energyict.protocolimpl.din19244.poreg2.request.register.*;

import java.io.IOException;
import java.util.*;

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

    public List<ProfileDataEntry> readProfileData(int length, ProfileDescription description, int registerAddress, int fieldAddress, Date lastReading, Date toDate) throws IOException {
        ProfileData profileData = new ProfileData(length, description.getGid(), poreg, registerAddress, fieldAddress, 1, 1, description.getProfileId(), lastReading, toDate);
        profileData.doRequest();
        return profileData.getProfileDataEntries();
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
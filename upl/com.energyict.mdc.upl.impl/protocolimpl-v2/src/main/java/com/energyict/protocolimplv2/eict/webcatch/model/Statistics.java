package com.energyict.protocolimplv2.eict.webcatch.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by H251019 on 15/11/2018.
 * This model is used to show the statistics of the messages that are processed by webcatch.
 */
@XmlRootElement
public class Statistics {
    private static Statistics instance;
    private int numberOfErrorMsgs;
    private int numberOfSuccessMsgs;
    private int numberOfMsgCount;
    private Date startedSince;
    private String eiserverVersion;
    private String webcatchVersion;
    private String rootName;

    /**Creates a singleton instance of the object. */
    private Statistics(){
        this.startedSince = new Date();
        this.webcatchVersion ="v1";
    }

    public static Statistics getInstance(){
        if(instance ==null){
            instance = new Statistics();
        }
        return instance;
    }

    public Date getStartedSince() {
        return startedSince;
    }

    @XmlElement
    public void setStartedSince(Date startedSince) {
        this.startedSince = startedSince;
    }

    public int getNumberOfErrorMsgs() {
        return numberOfErrorMsgs;
    }

    @XmlElement
    public void setNumberOfErrorMsgs(int numberOfErrorMsgs) {
        this.numberOfErrorMsgs = numberOfErrorMsgs;
    }

    public int getNumberOfSuccessMsgs() {
        return numberOfSuccessMsgs;
    }

    @XmlElement
    public void setNumberOfSuccessMsgs(int numberOfSuccessMsgs) {
        this.numberOfSuccessMsgs = numberOfSuccessMsgs;
    }

    public int getNumberOfMsgCount() {
        return numberOfMsgCount;
    }

    @XmlElement
    public void setNumberOfMsgCount(int numberOfMsgCount) {
        this.numberOfMsgCount = numberOfMsgCount;
    }

    public String getEiserverVersion() {
        return eiserverVersion;
    }

    @XmlElement
    public void setEiserverVersion(String eiserverVersion) {
        this.eiserverVersion = eiserverVersion;
    }

    public void incrementErrorMsgs(){
        numberOfErrorMsgs++;
    }

    public void incrementSuccessfulMsgs(){
        numberOfSuccessMsgs++;
    }

    public void incrementMsgCount(){
        numberOfMsgCount++;
    }


    public String getWebcatchVersion() {
        return webcatchVersion;
    }

    @XmlElement
    public void setWebcatchVersion(String webcatchVersion) {
        this.webcatchVersion = webcatchVersion;
    }


    public String getRootName() {
        return rootName;
    }

    @XmlElement
    public void setRootName(String rootName) {
        this.rootName = rootName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Statistics that = (Statistics) o;

        if (numberOfErrorMsgs != that.numberOfErrorMsgs) return false;
        if (numberOfSuccessMsgs != that.numberOfSuccessMsgs) return false;
        if (numberOfMsgCount != that.numberOfMsgCount) return false;
        if (!startedSince.equals(that.startedSince)) return false;
        if (!eiserverVersion.equals(that.eiserverVersion)) return false;
        if (!webcatchVersion.equals(that.webcatchVersion)) return false;
        return rootName.equals(that.rootName);

    }

    @Override
    public int hashCode() {
        int result = numberOfErrorMsgs;
        result = 31 * result + numberOfSuccessMsgs;
        result = 31 * result + numberOfMsgCount;
        result = 31 * result + startedSince.hashCode();
        result = 31 * result + eiserverVersion.hashCode();
        result = 31 * result + webcatchVersion.hashCode();
        result = 31 * result + rootName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "numberOfErrorMsgs=" + numberOfErrorMsgs +
                ", numberOfSuccessMsgs=" + numberOfSuccessMsgs +
                ", numberOfMsgCount=" + numberOfMsgCount +
                ", startedSince=" + startedSince +
                ", eiserverVersion='" + eiserverVersion + '\'' +
                ", webcatchVersion='" + webcatchVersion + '\'' +
                ", rootName='" + rootName + '\'' +
                '}';
    }

}

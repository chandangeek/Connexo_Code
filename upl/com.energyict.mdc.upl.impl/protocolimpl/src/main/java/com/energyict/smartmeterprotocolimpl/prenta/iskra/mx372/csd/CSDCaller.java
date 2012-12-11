package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.csd;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.shadow.CommunicationSchedulerShadow;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 31/01/12
 * Time: 16:30
 */

public class CSDCaller {

    private CommunicationScheduler csdCaller = null;
    private Device rtu;
    private int timeOut;
    private int pollFreq;
    private int csdCallTimeout;
    private boolean fixedIp = false;
    private String phone;
    private boolean itsNotClean = false;

    private Date lastStart;
    private Date lastEnd;
    private Date nextComm;

    /**
     * Constructor
     *
     * @param rtu
     */
    public CSDCaller(Device rtu) {
        this.rtu = rtu;
        this.timeOut = Integer.parseInt((String) this.rtu.getProperties().getProperty("PollTimeOut", "900000"));
        this.pollFreq = Integer.parseInt((String) this.rtu.getProperties().getProperty("CsdPollFrequency", "20000"));
        this.csdCallTimeout = Integer.parseInt((String) this.rtu.getProperties().getProperty("CsdCallTimeOut", "900000"));
//        this.phone = this.rtu.getPhoneNumber();
        this.fixedIp = ((String) this.rtu.getProperties().getProperty("FixedIpAddress", "0")).equalsIgnoreCase("1");
    }

    /**
     * Set the csdCommProfile to readNow and then poll for the ipAddress
     *
     * @return
     * @throws IOException
     * @throws BusinessException
     * @throws SQLException
     */
    public String doWakeUp() throws IOException, BusinessException, SQLException {
        try {

            findCSDSchedule();

            if (csdCaller != null) {


                this.lastStart = this.csdCaller.getLastCommunicationStart();
                this.lastEnd = this.csdCaller.getLastCommunicationEnd();
                this.nextComm = this.csdCaller.getNextCommunication();

                this.csdCaller.startReadingNow();

                if (checkForSuccessfulCSDCall()) {    // if the csdCall is successful then poll the radius server
                    if (fixedIp) {
//                        String ip = this.rtu.getIpAddress();
//                        if (!ip.equalsIgnoreCase("")) {
//                            return ip;
//                        } else {
                            throw new ConnectionException("There is no fixed IP address filled in.");
//                        }
                    } else {
                        IpUpdater ipUpdater = new IpUpdater(this.timeOut, this.pollFreq);
                        return ipUpdater.poll(this.phone, this.csdCaller.getLastCommunicationStart());
                    }
                } else {
                    return "";
                }

            } else {
                throw new IOException("Didn't find a CSD communication schedule.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error connecting to the database." + e.getMessage());
        } catch (BusinessException e) {
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
            if (!itsNotClean) {
                makeCleanCSDSchedule();
            }
        }
    }

    /**
     * Search for a communicationscheduler with 'csd' in the name, if you find more then one then throw error
     *
     * @throws IOException
     */
    private void findCSDSchedule() throws IOException {
//        this.csdCaller = null;
//        List allCommSchedules = rtu.getCommunicationSchedulers();
//        Iterator it = allCommSchedules.iterator();
//        while (it.hasNext()) {
//            CommunicationScheduler commSched = (CommunicationScheduler) it.next();
//            if (commSched.displayString().toLowerCase().indexOf("csd") > 0) {
//                if (this.csdCaller != null) {
//                    throw new IOException("Found more then one csdSchedule, only one can have a csd name.");
//                }
//                this.csdCaller = commSched;
//            }
//        }
    }

    /**
     * Poll when the csdCommunicationScheduler has been executed
     * The result code indicates whether we need to start polling for the ipAddress or not
     *
     * @return true when completionCode was CC_OK, false for all others
     * @throws IOException
     * @throws SQLException
     * @throws BusinessException
     */
    private boolean checkForSuccessfulCSDCall() throws IOException, SQLException, BusinessException {
        long waitTimeout = System.currentTimeMillis() + this.csdCallTimeout;
        Date newLastStart = this.csdCaller.getLastCommunicationStart();
        try {
            while (!isChanged(this.lastStart, newLastStart)) {
                Thread.sleep(20000);
                findCSDSchedule();
                newLastStart = this.csdCaller.getLastCommunicationStart();
                if (System.currentTimeMillis() > waitTimeout) {
                    throw new IOException("Waited more then " + this.csdCallTimeout / 60000 + " minutes before the WakeUp call started.");
                }
            }
            Date newLastEnd = this.csdCaller.getLastCommunicationEnd();
            Date newNextComm = this.csdCaller.getNextCommunication();

            while (!isCommunicationEnd(newLastStart, this.lastEnd, newLastEnd, this.nextComm, newNextComm)) {
                Thread.sleep(20000);
                findCSDSchedule();
                newLastEnd = this.csdCaller.getLastCommunicationEnd();
                newNextComm = this.csdCaller.getNextCommunication();
                if (System.currentTimeMillis() > waitTimeout) {
                    throw new IOException("Waited more then " + this.csdCallTimeout / 60000 + " minutes before the WakeUp call started.");
                }
            }

            makeCleanCSDSchedule();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.csdCaller.getLastCompletionCode() == 0;        //CC_OK
    }

    /**
     * Update the csdCaller with a clear communicationScheduler.
     * With a clear schedule we mean no nextcommunication date
     *
     * @throws SQLException
     * @throws BusinessException
     */
    private void makeCleanCSDSchedule() throws SQLException, BusinessException {
        if (this.csdCaller != null) {
            CommunicationSchedulerShadow cs = this.csdCaller.getShadow();
            cs.setNextCommunication(null);
            cs.setRetrials(0);
            this.csdCaller.update(cs);
            this.itsNotClean = true;
        }
    }

    private boolean isChanged(Date oldDate, Date newDate) {
        if (oldDate == null) {
            if (newDate == null) {
                return false;
            } else {
                return true;
            }
        } else {
            if (newDate == null) {
                return true;
            } else {
                return newDate.after(oldDate);
            }
        }
    }

    private boolean isCommunicationEnd(Date lastStart, Date oldEndTime, Date newEndTime, Date oldNextComm, Date newNextComm) {
        // communication failed when newNextComm is after lastStart
        if (newNextComm != null) {
            if (newNextComm.after(lastStart)) {
                return true;
            } else {
                return false;
            }
        } else {
            return isChanged(oldEndTime, newEndTime);
        }
    }
}
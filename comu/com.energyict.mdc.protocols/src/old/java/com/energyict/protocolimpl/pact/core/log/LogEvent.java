/*
 * LogEvent.java
 *
 * Created on 30 maart 2004, 11:17
 */

package com.energyict.protocolimpl.pact.core.log;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.pact.core.common.PactUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class LogEvent {

	private int main;
	private int sub;
	private int more;
	private Date date;
    private Date futureDate;

    /** Creates a new instance of LogHeader */
    public LogEvent(byte[] data,TimeZone timeZone) {
        parse(data,timeZone);
    }

    private void parse(byte[] data,TimeZone timeZone) {
        try {
            setMain(ProtocolUtils.byte2int(data[0]));
            setSub(ProtocolUtils.byte2int(data[1]));
            setMore(ProtocolUtils.getIntLE(data,2,2));
            setDate(verifyDate(PactUtils.getCalendar(ProtocolUtils.getIntLE(data,4,2),ProtocolUtils.getIntLE(data,6,2),timeZone).getTime(),timeZone));
        }
        catch (IOException e) {
            e.printStackTrace(); // should never happen!
        }
    }

    private Date verifyDate(Date dat,TimeZone timeZone) {
        Date now = ProtocolUtils.getCalendar(timeZone).getTime();
        if (now.before(dat)) {
            setFutureDate(dat);
            return now;
        }
        else {
            setFutureDate(null);
            return dat;
        }
    }

    /** Getter for property main.
     * @return Value of property main.
     *
     */
    public int getMain() {
        return main;
    }

    /** Setter for property main.
     * @param main New value of property main.
     *
     */
    public void setMain(int main) {
        this.main = main;
    }

    /** Getter for property sub.
     * @return Value of property sub.
     *
     */
    public int getSub() {
        return sub;
    }

    /** Setter for property sub.
     * @param sub New value of property sub.
     *
     */
    public void setSub(int sub) {
        this.sub = sub;
    }

    /** Getter for property more.
     * @return Value of property more.
     *
     */
    public int getMore() {
        return more;
    }

    /** Setter for property more.
     * @param more New value of property more.
     *
     */
    public void setMore(int more) {
        this.more = more;
    }

    /** Getter for property date.
     * @return Value of property date.
     *
     */
    public java.util.Date getDate() {
        return date;
    }

    /** Setter for property date.
     * @param date New value of property date.
     *
     */
    public void setDate(java.util.Date date) {
        this.date = date;
    }

    public Date getFutureDate() {
        return futureDate;
    }

    public void setFutureDate(Date futureDate) {
        this.futureDate = futureDate;
    }

}

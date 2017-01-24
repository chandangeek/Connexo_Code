/*
 * MaximumDemand.java
 *
 * Created on 10 juni 2004, 14:01
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
/*
 * @author  Koen
 */
public class MaximumDemand extends MainRegister implements Comparable {

    final public int ON_QUANTITY=0;
    final public int ON_DATETIME=1;


    int regSource;
    Date dateTime;
    TimeZone timeZone;
    int sorter;

    /** Creates a new instance of MaximumDemand */
    public MaximumDemand() {
    }

    public MaximumDemand(byte[] data,TimeZone timeZone) throws IOException {
        super();
        this.timeZone=timeZone;
        parse(data);
    }

    // TODO ?? energy of demand ??
    private void parse(byte[] data) throws IOException {
        long shift = (long)ProtocolUtils.getIntLE(data,0,4)&0xFFFFFFFFL;
        setDateTime(ProtocolUtils.getCalendar(timeZone,shift).getTime());
        setRegSource(ProtocolUtils.getIntLE(data,4,1));
        BigDecimal bd = BigDecimal.valueOf(Long.parseLong(Long.toHexString(ProtocolUtils.getLongLE(data,5,7))));
        setQuantity(new Quantity(bd,EnergyTypeCode.getUnitFromRegSource(getRegSource(),false)));
    }

    public String toString() {
        return "MD register: quantity="+getQuantity()+", regSource="+getRegSource()+", dateTime="+getDateTime().toString();
    }


    /**
     * Getter for property regSource.
     * @return Value of property regSource.
     */
    public int getRegSource() {
        return regSource;
    }

    /**
     * Setter for property regSource.
     * @param regSource New value of property regSource.
     */
    public void setRegSource(int regSource) {
        this.regSource = regSource;
    }

    /**
     * Getter for property dateTime.
     * @return Value of property dateTime.
     */
    public java.util.Date getDateTime() {
        return dateTime;
    }

    /**
     * Setter for property dateTime.
     * @param dateTime New value of property dateTime.
     */
    public void setDateTime(java.util.Date dateTime) {
        this.dateTime = dateTime;
    }

    static void sortOnQuantity(List list) throws IOException {
        try {
            Iterator it = list.iterator();
            while(it.hasNext()) {
                MaximumDemand md = (MaximumDemand)it.next();
                md.setSorter(md.ON_QUANTITY);
            }
            Collections.sort(list);
        }
        catch(ClassCastException e) {
            throw new NoSuchRegisterException( "MaximumDemand, sortOnQuantity, Error sorting maximum demand registers cause units do not match!");
        }
    }
    static void sortOnDateTime(List list) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            MaximumDemand md = (MaximumDemand)it.next();
            md.setSorter(md.ON_DATETIME);
        }
        Collections.sort(list);
    }

    public int compareTo(Object o) {
        if (getSorter() == ON_QUANTITY)
            return (getQuantity().compareTo(((MaximumDemand)o).getQuantity()));
        else if (getSorter() == ON_DATETIME)
            return (getDateTime().compareTo(((MaximumDemand)o).getDateTime()));
        return 0;
    }

    /**
     * Getter for property sorter.
     * @return Value of property sorter.
     */
    public int getSorter() {
        return sorter;
    }

    /**
     * Setter for property sorter.
     * @param sorter New value of property sorter.
     */
    public void setSorter(int sorter) {
        this.sorter = sorter;
    }

}

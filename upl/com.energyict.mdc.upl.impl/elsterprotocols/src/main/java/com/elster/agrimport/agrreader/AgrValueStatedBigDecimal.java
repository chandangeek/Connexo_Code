/* File:        
 * $HeadURL: http://deosn1-svnsv1.kromschroeder.elster-group.com/svn/eWorkPad/trunk/Libraries/ElsterAgrImport/src/com/elster/agrimport/agrreader/AgrValueStatedBigDecimal.java $
 * Version:     
 * $Id: AgrValueStatedBigDecimal.java 1787 2010-07-26 13:12:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  01.07.2010 14:11:17
 */
package com.elster.agrimport.agrreader;

import java.math.BigDecimal;

/**
 * This class extends AgrValueBigDecimal with an status.
 *
 * @author osse
 */
@SuppressWarnings({"unused"})
public class AgrValueStatedBigDecimal extends AgrValueBigDecimal implements IStatedAgrValue {
    int status;

    AgrValueStatedBigDecimal(BigDecimal bigDecimal, int status) {
        super(bigDecimal);
        this.status = status;
    }

    public AgrValueStatedBigDecimal() {
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        final AgrValueStatedBigDecimal other = (AgrValueStatedBigDecimal) obj;
        return this.status == other.status;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 59 * hash + this.status;
        return hash;
    }

}

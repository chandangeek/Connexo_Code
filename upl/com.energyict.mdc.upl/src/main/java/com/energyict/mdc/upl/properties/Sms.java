package com.energyict.mdc.upl.properties;

import java.util.Date;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (15:02)
 */
public interface Sms {

    String getId();

    Date getDate();

    String getFrom();

    String getTo();

    String getNetwork();

    byte[] getMessage();

}
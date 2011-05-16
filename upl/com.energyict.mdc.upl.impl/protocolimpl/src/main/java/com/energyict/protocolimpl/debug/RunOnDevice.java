package com.energyict.protocolimpl.debug;

import com.energyict.dialer.core.SerialCommunicationChannel;

import java.lang.annotation.*;

/**
 * Copyrights EnergyICT
 * Date: 12/05/11
 * Time: 15:36
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RunOnDevice {

    Class protocolClass();
    String commPort() default "";
    int baudRate() default 9600;
    int databits() default SerialCommunicationChannel.DATABITS_8;
    int parity() default SerialCommunicationChannel.PARITY_NONE;
    int stopbits() default SerialCommunicationChannel.STOPBITS_1;
    String phoneNumber() default "";
    String modemInit() default "ATM0";
    boolean showModeAscii() default false;
    boolean showMode7E1() default false;
    boolean showCommunication() default false;
    String observerFileName() default "";
    String timeZone() default "";

}

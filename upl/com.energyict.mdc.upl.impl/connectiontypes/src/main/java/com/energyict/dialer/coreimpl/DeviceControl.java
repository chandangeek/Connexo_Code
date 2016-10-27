/*
 * DeviceControl.java
 *
 * Created on 26 juli 2007, 11:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.coreimpl;

import java.io.IOException;

/**
 * @author kvds
 */
public interface DeviceControl {

    public void openPort0() throws IOException;

    public void openPort1() throws IOException;

    public void openPort2() throws IOException;

    public void openPort3() throws IOException;

    public void mpioSetStatus(int mpio) throws IOException;

    public int mpioGetStatus() throws IOException;

    public void rs485Mode(String port) throws IOException;

    public void rs485Mode(String port, int delay) throws IOException;
}

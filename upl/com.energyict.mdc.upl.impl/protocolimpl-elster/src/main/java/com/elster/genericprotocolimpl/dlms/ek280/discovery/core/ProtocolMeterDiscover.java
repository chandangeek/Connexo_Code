/*
 * ProtocolMeterDiscover.java
 *
 * Created on 1 juni 2005, 10:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.elster.genericprotocolimpl.dlms.ek280.discovery.core;

import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.LinkException;

import java.io.IOException;

import static com.energyict.protocolimpl.utils.ProtocolTools.delay;

/**
 * @author Koen
 */
public class ProtocolMeterDiscover {

    private static final int TIMEOUT = 2000;
    private final Link link;

    /**
     * Creates a new instance of ProtocolMeterDiscover
     */
    public ProtocolMeterDiscover(Link link) {
        this.link = link;
    }

    public Parameters receiveParametersFromMeter() throws LinkException, IOException {

        int timeoutCnt = 0;
        byte[] buffer = new byte[256];
        StringBuffer strBuff = new StringBuffer();
        while (true) {

            int count = link.getInputStream().available();
            if (count > 0) {
                int ret = link.getInputStream().read(buffer, 0, count > buffer.length ? buffer.length : count);
                if (ret == -1) { // EOF
                    throw new IOException("Premature end of protocol request session caused by EOF...");
                }
                strBuff.append(new String(buffer, 0, ret));

                if (strBuff.toString().indexOf("</REQUEST>") != -1) {
                    RequestParameters parameters = new RequestParameters(strBuff.toString());
                    link.getStreamConnection().setComPort(parameters.getComPort());
                    if (parameters.getComPort() != null) {
                        link.connect(); // KV 12012006
                    }
                    return parameters;
                } else if (strBuff.toString().indexOf("</DEPLOY>") != -1) {
                    DeployParameters parameters = new DeployParameters(strBuff.toString());
                    link.getStreamConnection().setComPort(parameters.getComPort());
                    if (parameters.getComPort() != null) {
                        link.connect(); // KV 12012006
                    }
                    return parameters;
                } else if (strBuff.toString().indexOf("</EVENT>") != -1) {
                    EventParameters parameters = new EventParameters(strBuff.toString());
                    link.getStreamConnection().setComPort(parameters.getComPort());
                    if (parameters.getComPort() != null) {
                        link.connect();
                    }
                    return parameters;
                } else if (strBuff.toString().indexOf("</EVENTPO>") != -1) {
                    EventPOParameters parameters = new EventPOParameters(strBuff.toString());
                    link.getStreamConnection().setComPort(parameters.getComPort());
                    if (parameters.getComPort() != null) {
                        link.connect();
                    }
                    return parameters;
                }
            } else {
                if (timeoutCnt++ > TIMEOUT) { // Timeout
                    throw new IOException("ProtocolMeterDiscover, receiveParametersFromMeter, premature end of protocol request session caused by timeout...");
                }
                delay(100);
            }
        }
    }
}

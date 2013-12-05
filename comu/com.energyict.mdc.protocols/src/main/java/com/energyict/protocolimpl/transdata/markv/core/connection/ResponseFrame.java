/*
 * ResponseFrame.java
 *
 * Created on 9 augustus 2005, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.connection;

import com.energyict.protocolimpl.base.ProtocolConnectionException;

/**
 *
 * @author koen
 */
public class ResponseFrame {

    static private final String[] prompts = {"Ok","failed","Retry","denied","disabled"};
    static private final int OK=0;
    static private final int FAILED=1;
    static private final int RETRY=2;
    static private final int DENIED=3;
    static private final int DISABLED=4;

    String strData;

    /** Creates a new instance of ResponseFrame */
    public ResponseFrame(String strData) {
        this.strData=strData;
    }

    public boolean isOK() {
        return (strData.indexOf(prompts[OK])>=0);
    }
    public boolean isRetry() {
        return (strData.indexOf(prompts[RETRY])>=0);
    }
    public boolean isDenied() {
        return (strData.indexOf(prompts[DENIED])>=0);
    }
    public boolean isFailed() {
        return (strData.indexOf(prompts[FAILED])>=0);
    }
    public boolean isDisabled() {
        return (strData.indexOf(prompts[DISABLED])>=0);
    }

    public String getPrompt() throws ProtocolConnectionException {
        if (isOK()) return prompts[OK];
        else if (isRetry()) return prompts[RETRY];
        else if (isDenied()) return prompts[DENIED];
        else if (isFailed()) return prompts[FAILED];
        else if (isDisabled()) return prompts[DISABLED];
        else throw new ProtocolConnectionException("ResponseFrame, getPrompt(), unknown prompt!");
    }

    public String getStrData() {
        return strData;
    }

    public void setStrData(String strData) {
        this.strData = strData;
    }

}

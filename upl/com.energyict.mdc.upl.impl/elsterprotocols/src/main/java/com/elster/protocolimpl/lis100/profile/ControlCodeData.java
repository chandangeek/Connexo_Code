package com.elster.protocolimpl.lis100.profile;

import java.util.Date;

/**
 * Class containing additional data for control codes
 *
 * User: heuckeg
 * Date: 07.02.11
 * Time: 12:07
 */
@SuppressWarnings({"unused"})
public class ControlCodeData {

    private int cc;
    private int subCode;
    private int len;
    private Date date;
    private Object[] params;

    public ControlCodeData(int controlCode) {
        this.cc = controlCode;
        this.subCode = 0;
        this.date = null;
        this.params = null;
        this.len = 1;
    }

    public int getParamCnt() {
        return (params == null) ? 0 : params.length;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public boolean hasDate() {
        return date != null;
    }

    public int getCc() {
        return cc;
    }

    public int getSubCode() {
        return this.subCode;
    }

    public void setSubCode(int subCode) {
        this.subCode = subCode;
    }

    public int length() {
        return this.len;
    }

    public void addLength(int words) {
        len += words;
    }

}

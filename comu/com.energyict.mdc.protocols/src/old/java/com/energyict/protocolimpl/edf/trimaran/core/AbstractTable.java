/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AbstractTable.java
 *
 * Created on 23 juni 2006, 15:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractTable {

    final int DEBUG=0;

    abstract protected void parse(byte[] data) throws IOException;
    abstract protected int getCode();
    private int length;

    private DataFactory dataFactory;

    /** Creates a new instance of AbstractTable */
    public AbstractTable(DataFactory dataFactory) {
        this.setDataFactory(dataFactory);
        setLength(0);
    }

    public DataFactory getDataFactory() {
        return dataFactory;
    }

    private void setDataFactory(DataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public void invoke() throws IOException {
        // KV 09082006 retry mechanism
        int retries=0;
        while(true) {
            try {
                parse(getDataFactory().getTrimeran().getSPDUFactory().enq(getCode(),getLength()).getData());
                break;
            }
            catch(IOException e) {
                if (retries++>=3)
                    throw new IOException(e.toString()+", "+getLogInfo());
                else if (DEBUG>=1) System.out.println("KV_DEBUG> AbstractTable, invoke(), "+e.toString()+", retry "+retries);
            }
            catch(ArrayIndexOutOfBoundsException e) {
                if (retries++>=3)
                    throw new IOException(e.toString()+", "+getLogInfo());
                else if (DEBUG>=1) System.out.println("KV_DEBUG> AbstractTable, invoke(), "+e.toString()+", retry "+retries);
            }
        } // while(true)

    } // public void invoke() throws IOException

    private String getLogInfo() {
        switch(getCode()) {
            case 4:
                return "requesting load profile data";
            case 2:
                return "requesting current month registers";
            case 1:
                return "requesting previous month registers";
            case 3:
                return "requesting meter status";

            default:
                return "unknown requesting code "+getCode();
        }
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

}

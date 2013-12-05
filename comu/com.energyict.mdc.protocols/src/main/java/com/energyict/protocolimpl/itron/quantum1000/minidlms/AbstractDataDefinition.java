/*
 * AbstractDataDefinition.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractDataDefinition {

    private final int DEBUG=0;

    abstract protected void parse(byte[] data) throws IOException;
    abstract protected int getVariableName();

    private DataDefinitionFactory dataDefinitionFactory=null;

    private int range;
    private int index;

    /** Creates a new instance of AbstractDataDefinition */
    public AbstractDataDefinition(DataDefinitionFactory dataDefinitionFactory) {
        this.setDataDefinitionFactory(dataDefinitionFactory);
        setRange(-1);
        setIndex(-1);
    }

    public void invokeRead() throws IOException {
        int retry=0;
        while(true) {
            try {
                ReadReply readReply=null;
                if (getRange()>=0) {
                   if (getIndex() ==-1)
                       setIndex(0);
                   readReply = getDataDefinitionFactory().getProtocolLink().getApplicationStateMachine().read(getVariableName(),getIndex(),getRange());
                }
                else if (getIndex() >= 0)
                   readReply = getDataDefinitionFactory().getProtocolLink().getApplicationStateMachine().read(getVariableName(),getIndex());
                else
                   readReply = getDataDefinitionFactory().getProtocolLink().getApplicationStateMachine().read(getVariableName());
                parse(readReply.getData());
                return;
            }
            catch(ReplyException e) {
                ReadReplyDataError error = (ReadReplyDataError)e.getAbstractReplyDataError();
                if (DEBUG>=1) System.out.println(error);
                if (retry++ >= 2) {
                    throw e; //new IOException("AbstractDataDefinition, invoke, "+e.toString());
                }
            }
        }
    }

    protected byte[] prepareBuild() {
        return null;
    }

    public void invokeWrite() throws IOException {
        int retry=0;
        while(true) {
            try {
                WriteReply writeReply = getDataDefinitionFactory().getProtocolLink().getApplicationStateMachine().write(getVariableName(),prepareBuild());
                return;
            }
            catch(ReplyException e) {
                WriteReplyDataError error = (WriteReplyDataError)e.getAbstractReplyDataError();
                if (DEBUG>=1) System.out.println(error);
                if (retry++ >= 2) {
                    throw e;
                }
            }
        }
    }

//    public void invokeUpload() {
//        int retry=0;
//        while(true) {
//            try {
//                WriteReply writeReply = getDataDefinitionFactory().getProtocolLink().getApplicationStateMachine().write(getVariableName(),prepareBuild());
//                return;
//            }
//            catch(ReplyException e) {
//                WriteReplyDataError error = (WriteReplyDataError)e.getAbstractReplyDataError();
//                if (DEBUG>=1) System.out.println(error);
//                if (retry++ >= 2) {
//                    throw e;
//                }
//            }
//        }
//    }

    public DataDefinitionFactory getDataDefinitionFactory() {
        return dataDefinitionFactory;
    }

    private void setDataDefinitionFactory(DataDefinitionFactory dataDefinitionFactory) {
        this.dataDefinitionFactory = dataDefinitionFactory;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }



}

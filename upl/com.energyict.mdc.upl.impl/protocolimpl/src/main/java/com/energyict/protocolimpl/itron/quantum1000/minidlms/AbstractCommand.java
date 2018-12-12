/*
 * AbstractCommand.java
 *
 * Created on 1 december 2006, 13:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractCommand {
    
    private final int DEBUG=0;
    
    private CommandFactory commandFactory;
    
    
    abstract protected byte[] prepareInvoke();
    
    private AbstractCommandResponse response;
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CommandFactory commandFactory) {
        this.setCommandFactory(commandFactory);
    }

    public void invoke() throws IOException {
        byte[] data2Send =  prepareInvoke();
        
        // send command to meter using datalink...
        byte[] data = getCommandFactory().getProtocolLink().getMiniDLMSConnection().sendCommand(data2Send);
        
        
        if (((int)data[0]&0xff) == AbstractCommandResponse.getREAD_RESPONSE()) {
            if ((data.length >=4) && ((data[2] == 0)&(data[3] == 0x0b))) {
                
if (DEBUG>=1) System.out.println("ReadReply "+ ProtocolUtils.outputHexString(data));
        
                setResponse(new ReadReply());
                getResponse().parse(data);
            }
            else {

if (DEBUG>=1) System.out.println("ReadReplyDataError"+ProtocolUtils.outputHexString(data));

                setResponse(new ReadReplyDataError());
                getResponse().parse(data);
                throw new ReplyException((AbstractReplyError)getResponse());
            }
            
        }  // if (((int)data[0]&0xff) == AbstractCommandResponse.getREAD_RESPONSE())       
        else if (((int)data[0]&0xff) == AbstractCommandResponse.getWRITE_RESPONSE()) {
            if (data.length < 3) {
                setResponse(new WriteReply());
                getResponse().parse(data);
            }
            else {
                setResponse(new WriteReplyDataError());
                getResponse().parse(data);
                throw new ReplyException((AbstractReplyError)getResponse());
            }
            
        }
        else if (((int)data[0]&0xff) == AbstractCommandResponse.getLOGGED_OFF()) {
            setResponse(new LoggedOffReply());
            getResponse().parse(data);
        }
        else if (((int)data[0]&0xff) == AbstractCommandResponse.getLOGGED_OFF()) {
            setResponse(new LoggedOffReply());
            getResponse().parse(data);
        }
        else if (((int)data[0]&0xff) == AbstractCommandResponse.getINITIATE_RESPONSE()) {
            setResponse(new InitiateResponse());
            getResponse().parse(data);
        }
        else if (((int)data[0]&0xff) == AbstractCommandResponse.getCONFIRMED_SERVICE_RESPONSE()) {
            if (data[1] == 0x18) {
                setResponse(new InitiateUploadResponse());
                getResponse().parse(data);
            }
            else if (data[1] == 0x16) {
                setResponse(new UploadSegmentResponse());
                getResponse().parse(data);
            }
            else if (data[1] == 0x17) {
                setResponse(new TerminateLoadResponse());
                getResponse().parse(data);
            }
            else if (data.length == 2) {
                setResponse(new ConfirmedServiceError());
                getResponse().parse(data);
                throw new ReplyException((AbstractReplyError)getResponse());
            }
        }
        
        
    } // public void invoke()
    
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public AbstractCommandResponse getResponse() {
        return response;
    }

    public void setResponse(AbstractCommandResponse response) {
        this.response = response;
    }


    
} // abstract public class AbstractCommand

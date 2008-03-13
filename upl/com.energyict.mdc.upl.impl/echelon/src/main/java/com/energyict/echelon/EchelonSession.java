package com.energyict.echelon;

import com.energyict.echelon.stub.*;
import com.energyict.xml.xmlhelper.DomHelper;

import java.util.*;

import javax.xml.rpc.ServiceException;

import org.w3c.dom.*;

/**
 * Function calls to Nes are always attempted twice
 * 
 * For every function call a session key is required.  This session key is 
 * only valid for a fixed period of time.  Therefore it can happen that the 
 * session with Nes has expired.  
 * 
 * When this happens a new session has to be initialized.  Therefore when a 
 * call fails, a new session is initialized, and the call is retried.
 *  
 * @author fbo
 */

class EchelonSession {

    private static HashMap<String, EchelonSession> instances = new HashMap<String, EchelonSession>();
    
    private UserManagerSoap_PortType userManager;
    private DeviceManagerSoap_PortType deviceManager;
    private GatewayManagerSoap_PortType gatewayManager;
    private MessageLogManagerSoap_PortType messageLogManager;
    private TimeZoneManagerSoap_PortType timezoneManager;
    private TaskManagerSoap_PortType taskManager;
    
    static EchelonSession getInstance(String serverUri) throws EchelonException {
        if( instances.get(serverUri) == null ) {
            initSession(serverUri);
        }
        return instances.get(serverUri);
    }
    
    static void initSession(String serverUri ) throws EchelonException {
        try {
            synchronized(EchelonSession.class) {
            	if (instances.get(serverUri) == null) {
            		instances.put(serverUri, new EchelonSession(serverUri));
            	}
            }
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new EchelonException(e);
        }
    }
    
    private EchelonSession(String serverUri) throws ServiceException {
        
        UserManagerLocator aUserManager = new UserManagerLocator();
        aUserManager.setUserManagerSoapEndpointAddress(getUserUri(serverUri));
        this.userManager = aUserManager.getUserManagerSoap(); 
            
        DeviceManagerLocator aDeviceManager = new DeviceManagerLocator();
        aDeviceManager.setDeviceManagerSoapEndpointAddress(getDeviceUri(serverUri));
        this.deviceManager = aDeviceManager.getDeviceManagerSoap();
        
        GatewayManagerLocator aGatewayManager = new GatewayManagerLocator();
        aGatewayManager.setGatewayManagerSoapEndpointAddress(getGatewayUri(serverUri));
        this.gatewayManager = aGatewayManager.getGatewayManagerSoap();
        
        MessageLogManagerLocator aMessageLogManager = new MessageLogManagerLocator();
        aMessageLogManager.setMessageLogManagerSoapEndpointAddress(getMessageLogUri(serverUri));
        this.messageLogManager = aMessageLogManager.getMessageLogManagerSoap();
        
        TimeZoneManagerLocator aTimeZoneManager = new TimeZoneManagerLocator();
        aTimeZoneManager.setTimeZoneManagerSoapEndpointAddress(getTimezoneUri(serverUri));
        this.timezoneManager = aTimeZoneManager.getTimeZoneManagerSoap();
        
        TaskManagerLocator aTaskManager = new TaskManagerLocator();
        aTaskManager.setTaskManagerSoapEndpointAddress(getTaskManagerUri(serverUri));
        this.taskManager = aTaskManager.getTaskManagerSoap();

    }
    
    /** concatenate server address and UserManager address */
    private String getUserUri(String serverUri) {
        return "http://" + serverUri + "/CoreServices/UserManager.asmx";
    }
    
    /** concatenate server address and DeviceManager address */
    private String getDeviceUri(String serverUri) {
        return "http://" + serverUri + "/CoreServices/DeviceManager.asmx";
    }
    
    /** concatenate server address and DeviceManager address */
    private String getGatewayUri(String serverUri) {
        return "http://" + serverUri + "/CoreServices/GatewayManager.asmx";
    }
    
    /** concatenate server address and DeviceManager address */
    private String getMessageLogUri(String serverUri) {
        return "http://" + serverUri + "/CoreServices/MessageLogManager.asmx";
    }
    
    /** concatenate server address and TimezoneManager address */
    private String getTimezoneUri(String serverUri) {
        return "http://" + serverUri + "/CoreServices/TimezoneManager.asmx";
    }    
    
    /** concatenate server address and TimezoneManager address */
    private String getTaskManagerUri(String serverUri) {
        return "http://" + serverUri + "/CoreServices/TaskManager.asmx";
    }
    
    
    /**
     * Get a session id for connecting to the Echelon NES system.
     * @return the echelon key returned by NES.
     * @throws EchelonException
     */
    public String getSession() throws EchelonException {
        
        try {
            
        	String sessionKey;
            String cmd = Constants.UserAuthenticationTypes.DEFAULT;
            String r;
            
            /* User authentication should be done on the webservice level.
             * This login call just generates a key that is used for encoding
             * the messages send to the NES application server. */
            
            r = userManager.login("", "", cmd);
            
            Node n = Util.toDom(r).getElementsByTagName("APIKEY").item(0);
            sessionKey = n.getChildNodes().item(0).getNodeValue();
            
            return sessionKey;

        } catch (Exception e) {
            e.printStackTrace();
            throw new EchelonException(e);
        } 
        
    }
    
    Document retrieveResultList(String par, String sessionId) throws EchelonException {
        Document d = null;
        try {
            d = Util.toDom(deviceManager.retrieveResultList(sessionId, par));
            if( Util.isSucceeded(d) ) return d;
            
            d = Util.toDom(deviceManager.retrieveResultList(sessionId, par));
            Util.checkStatus(d);
            return d;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new EchelonException(e);
        }
    }
    
    Document retrieveResult(String id, String sessionId) throws EchelonException {
        Document d = null;
        try {
            d = Util.toDom(deviceManager.retrieveResult(sessionId, id));
            if( Util.isSucceeded(d) ) return d;

            d = Util.toDom(deviceManager.retrieveResult(sessionId, id));
            Util.checkStatus(d);
            return d;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new EchelonException(e);
        }
    }
    
    Document deleteResultList(String param, String sessionId) throws EchelonException {
        Document d = null;
        try {
            d = Util.toDom(gatewayManager.deleteResultList(sessionId,param));
            if( Util.isSucceeded(d) ) return d;
            
            d = Util.toDom(gatewayManager.deleteResultList(sessionId,param));
            Util.checkStatus(d);
            return d;
            
        } catch(Exception e){
            e.printStackTrace();
            throw new EchelonException(e);
        }
    }
    
    Document performCommand(String param, String sessionId) throws EchelonException {
        Document d = null;
        try {
            d = Util.toDom( deviceManager.performCommand(sessionId, param) );
            if( Util.isSucceeded(d) ) return d;

            d = Util.toDom(deviceManager.performCommand(sessionId, param));
            Util.checkStatus(d);
            return d;
            
        } catch(Exception e){
            e.printStackTrace();
            throw new EchelonException(e);
        }
    }
    
    Document register(String param, String sessionId) throws EchelonException {
        Document d = null;
        try {
            d = Util.toDom( gatewayManager.register(sessionId, param) );
            if( Util.isSucceeded(d) ) return d;
            
            d = Util.toDom(gatewayManager.register(sessionId, param));
            Util.checkStatus(d);
            return d;
        } catch(Exception x){
            x.printStackTrace();
            throw new EchelonException(x);
        }
    }
    
    Document createDevice(String param, String sessionId) throws EchelonException {
        Document d = null;
        try {
            d = Util.toDom( deviceManager.create(sessionId, param) );
            if( Util.isSucceeded(d) ) return d;

            d = Util.toDom(deviceManager.create(sessionId, param) );
            Util.checkStatus(d);
            
            return d;
        } catch(Exception x){
            x.printStackTrace();
            throw new EchelonException(x);
        }        
    }
    
    Document retrieveCommandHistory(String taskId, String sessionId) throws EchelonException {
        
        Document d = null;
        
        DomHelper builder = new DomHelper("PARAMETERS");
        builder.addElement("COMMANDHISTORYID", taskId);
        Element returnElement = builder.addElement("RETURN");
        builder.addElement(returnElement, "GENERALINFORMATION" );
        
        String param =  Util.scrubHeader(builder.toXmlString()); 
        
        try {
            d = Util.toDom( deviceManager.retrieveCommandHistory(sessionId, param) ); 
                    
            //"<PARAMETERS><COMMANDHISTORYID>7f906df007ac4d90ba8ef903ddfef384</COMMANDHISTORYID><RETURN><GENERALINFORMATION/></RETURN></PARAMETERS>" ) );
            if( Util.isSucceeded(d) ) return d;

            d = Util.toDom( deviceManager.retrieveCommandHistory(sessionId, param) );
            Util.checkStatus(d);
            
            return d;
        } catch(Exception x){
            x.printStackTrace();
            throw new EchelonException(x);
        }        
    }
        
    
    String retrieveMsgLog(String param, String sessionId) throws EchelonException {
        try {
            String s = messageLogManager.retrieveList(sessionId, param);
            return s;
        } catch(Exception x){
            x.printStackTrace();
            throw new EchelonException(x);
        }        
    }
    
    Document getTimeZoneList(String sessionId) throws EchelonException {
        Document d = null;
        try {
            
            d = Util.toDom( timezoneManager.retrieveList( sessionId ) );
            if( Util.isSucceeded(d) ) return d;
            
            d = Util.toDom( timezoneManager.retrieveList( sessionId ) );
            Util.checkStatus(d);
            return d;
            
        } catch(Exception x){
            x.printStackTrace();
            throw new EchelonException(x);
        }        
    }
    
    /* Name NES uses for CET timezone */
    private static final Map timezoneMap = new HashMap() {{
        put( "CET", "Brussels, Copenhagen, Madrid, Paris" );
        put( "CEST", "Brussels, Copenhagen, Madrid, Paris" );
    }};
    
    /* Match the JAVA timezone to ness time zone.
     * For now only doing CET, adding others should be easy. */
    String getTimezone(String tz, String sessionId) throws EchelonException {

        try {

            String nesName = (String)timezoneMap.get(tz);
            
            Document d = getTimeZoneList(sessionId);
            
            String xpath = 
            "RETURNS/APIPAYLOAD/TIMEZONES/TIMEZONE[NAME='" + nesName + "']/ID";
            
            Element e = Util.xPath(d, xpath);
            
            if(e==null) {
                String msg = "Timezone: " + tz + " is not supported ";
                throw new EchelonException( msg );
            }
            
            return e.getFirstChild().getNodeValue();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new EchelonException( e );
        }
        
    }
    
    Document retrieveTask(String taskID, String sessionId) throws EchelonException {
        Document d = null;
        try {
            String param = "<PARAMETERS></PARAMETERS>";
            d = Util.toDom(taskManager.retrieveList(sessionId, param));
            if( Util.isSucceeded(d) ) return d;

            d = Util.toDom(taskManager.retrieve(sessionId, taskID, param));
            Util.checkStatus(d);
            return d;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new EchelonException(e);
        }
    }
    
    Document connect(String deviceId, String sessionId) throws EchelonException {

        Document d = null;
        try {

            DomHelper builder = new DomHelper("PARAMETERS");
            
            builder.addElement("COMMUNICATIONREQUESTTYPEID", Constants.GatewayCommunicationRequestTypes.SERVER_INITIATED_NORMAL_PRIORITY );
            builder.addElement("TIMEOUTDATETIME", Util.DATE_FORMAT.format( nextHour() ));
            
            
            String param = Util.scrubHeader( builder.toXmlString() ); 
            
            d = Util.toDom(gatewayManager.connect(sessionId, deviceId, param) );
            
            if( Util.isSucceeded(d) ) return d;

            d = Util.toDom(gatewayManager.performCommand(sessionId, param) );
            Util.checkStatus(d);
            return d;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new EchelonException(e);
        }
        
        
    }
    
    /** utility method for creating Date 1 month in the future */ 
    private Date nextHour( ) {
        Calendar c = Calendar.getInstance();
        c.add( Calendar.HOUR_OF_DAY, 1);
        return c.getTime();
    }
    
    
    public static void main(String[] args) throws Exception {
        EchelonSession s = EchelonSession.getInstance(args[1]);
//        String rslt = s.taskManager.retrieveList(s.getSession(), "<PARAMETERS></PARAMETERS>");
//        
//        System.out.println(rslt);
//        
        DomHelper builder = new DomHelper("PARAMETERS");
        
//        builder.addElement("COMMUNICATIONREQUESTTYPEID", Constants.GatewayCommunicationRequestTypes.SERVER_INITIATED_NORMAL_PRIORITY );
        builder.addElement("TIMEOUTDATETIME", Util.DATE_FORMAT.format( s.nextHour() ));
        
        String param = Util.scrubHeader( builder.toXmlString() ); 
        
        System.out.println(
        s.gatewayManager.disconnect(s.getSession(), args[2], param)
        );
    }
    
}

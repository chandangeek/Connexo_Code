/*
 * PPPDialer.java
 *
 * Created on 18 april 2002, 15:57
 */

package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 *
 * @author  Karel
 */

public class PPPDialer {
    
    private static Map errors = null;
    
    private String entryName;
    private String userName;
    private String password;
    private String domain;
    private String phone;
    private String callback;
    private String phonebook;
    
    private Logger logger;
    
    /** Creates a new instance of PPPDialer */
    public PPPDialer(String entryName, Logger logger) {
        this.entryName = entryName;
        this.logger = logger;
    }
    
    public void connect() throws IOException {
        String cmd = "rasdial " + entryName + " ";
        
        if( userName != null )  cmd += userName + " ";
        if( password != null )  cmd += password + " ";
        if( domain != null )    cmd += "/DOMAIN:" + domain + " ";
        if( phone != null )     cmd += "/PHONE:" + phone + " ";
        if( callback != null )  cmd += "/CALLBACK:" + callback + " ";
        if( phonebook != null ) cmd += "/PHONEBOOK:" + phonebook + " ";
        
        Process process = Runtime.getRuntime().exec( cmd );
        int result;
        try {
            result = process.waitFor();
        } 
        catch (InterruptedException ex) {
            getLogger().log(Level.SEVERE,entryName,ex);
            throw new IOException(ex.toString());
        }
        if (result != 0) {
            String m = "Rasdial error occured for \"" + entryName + "\": " 
                + describe(result); 
            getLogger().severe(m);
            throw new IOException("Rasdial return code " + describe(result));
        }
        return ;
    }
        
    public void disconnect() {
        int result = 0;
        try {    
            Process process = Runtime.getRuntime().exec("rasdial /DISCONNECT");
            result = process.waitFor();
        } 
        catch (IOException ex) {
            getLogger().log(Level.SEVERE,entryName,ex);
        } 
        catch (InterruptedException ex) {
            getLogger().log(Level.SEVERE,phone,ex);
        }
        if (result != 0) {
            String m = "Rasdial disconnect return code " + describe( result);
            getLogger().severe(m);
        }
        return;
    }
        
    protected Logger getLogger() {
        return logger;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public void setPhonebook(String phonebook) {
        this.phonebook = phonebook;
    }
    
    /* Compose a descriptive error message for a Ras dial exception */
    private String describe(int errorCode) {
        if( errors == null ) {
            errors = new HashMap();
            errors.put( "600", "An operation is pending.");
            errors.put( "601", "An invalid port handle was detected.");
            errors.put( "602", "The specified port is already open.");
            errors.put( "603", "The caller's buffer is too small.");
            errors.put( "604", "Incorrect information was specified.");
            errors.put( "605", "The port information cannot be set.");
            errors.put( "606", "The specified port is not connected.");
            errors.put( "607", "An invalid event is detected.");
            errors.put( "608", "A device was specified that does not exist.");
            errors.put( "609", "The device type was specified that does not exist.");
            errors.put( "610", "An invalid buffer was specified.");
            errors.put( "611", "A route was specified that is not available.");
            errors.put( "612", "A route was specified that is not allocated.");
            errors.put( "613", "An invalid compression was specified.");
            errors.put( "614", "There were insufficient buffers available.");
            errors.put( "615", "The specified port was not found.");
            errors.put( "616", "An asynchronous request is pending.");
            errors.put( "617", "The modem is already disconnecting.");
            errors.put( "618", "The specified port is not open.");
            errors.put( "619", "The specified port is not connected.");
            errors.put( "620", "No endpoints could be determined.");
            errors.put( "621", "The system could not open the phonebook.");
            errors.put( "622", "The system could not load the phonebook.");
            errors.put( "623", "The system could not find the phonebook entry for this connection.");
            errors.put( "624", "The system could not update the phonebook file.");
            errors.put( "625", "The system found invalid information in the phonebook.");
            errors.put( "626", "A string could not be loaded.");
            errors.put( "627", "A key could not be found.");
            errors.put( "628", "The connection was closed.");
            errors.put( "629", "The connection was closed by the remote computer.");
            errors.put( "630", "The modem was disconnected due to hardware failure.");
            errors.put( "631", "The user disconnected the modem.");
            errors.put( "632", "An incorrect structure size was detected.");
            errors.put( "633", "The modem is already in use or is not configured for dialing out.");
            errors.put( "634", "Your computer could not be registered on the remote network.");
            errors.put( "635", "There was an unknown error.");
            errors.put( "636", "The device attached to the port is not the one expected.");
            errors.put( "637", "A string was detected that could not be converted.");
            errors.put( "638", "The request has timed out.");
            errors.put( "639", "No asynchronous net is available.");
            errors.put( "640", "A error has occurred involving NetBIOS.");
            errors.put( "641", "The server cannot allocate NetBIOS resources needed to support the client.");
            errors.put( "642", "One of your computer's NetBIOS names is already registered on the remote network.");
            errors.put( "643", "A network adapter at the server failed.");
            errors.put( "644", "You will not receive network message popups.");
            errors.put( "645", "There was an internal authentication error.");
            errors.put( "646", "The account is not permitted to log on at this time of day.");
            errors.put( "647", "The account is disabled.");
            errors.put( "648", "The password for this account has expired.");
            errors.put( "649", "The account does not have permission to dial in.");
            errors.put( "650", "The remote access server is not responding.");
            errors.put( "651", "The modem has reported an error.");
            errors.put( "652", "There was an unrecognized response from the modem.");
            errors.put( "653", "A macro required by the modem was not found in the device .INF file section.");
            errors.put( "654", "A command or response in the device .INF file section refers to an undefined macro.");
            errors.put( "655", "The <MESSAGE> macro was not found in the device .INF file section.");
            errors.put( "656", "The <DEFAULTOFF> macro in the device .INF file section contains an undefined macro.");
            errors.put( "657", "The device .INF file could not be opened.");
            errors.put( "658", "The device name in the device .INF or media .INI file is too long.");
            errors.put( "659", "The media .INI file refers to an unknown device name.");
            errors.put( "660", "The device .INF file contains no responses for the command.");
            errors.put( "661", "The device .INF file is missing a command.");
            errors.put( "662", "There was an attempt to set a macro not listed in the device .INF file section.");
            errors.put( "663", "The media .INI file refers to an unknown device type.");
            errors.put( "664", "The system has run out of memory.");
            errors.put( "665", "The modem is not properly configured.");
            errors.put( "666", "The modem is not functioning.");
            errors.put( "667", "The system was unable to read the media .INI file.");
            errors.put( "668", "The connection was terminated.");
            errors.put( "669", "The usage parameter in the media .INI file is invalid.");
            errors.put( "670", "The system was unable to read the section name from the media .INI file.");
            errors.put( "671", "The system was unable to read the device type from the media .INI file.");
            errors.put( "672", "The system was unable to read the device name from the media .INI file.");
            errors.put( "673", "The system was unable to read the usage from the media .INI file.");
            errors.put( "674", "The system was unable to read the maximum connection BPS rate from the media .INI file.");
            errors.put( "675", "The system was unable to read the maximum carrier connection speed from the media .INI file.");
            errors.put( "676", "The phone line is busy.");
            errors.put( "677", "A person answered instead of a modem.");
            errors.put( "678", "There was no answer.");
            errors.put( "679", "The system could not detect the carrier.");
            errors.put( "680", "There was no dial tone.");
            errors.put( "681", "The modem reported a general error.");
            errors.put( "691", "Access was denied because the user name and/or password was invalid on the domain.");
            errors.put( "692", "There was a hardware failure in the modem.");
            errors.put( "695", "The state machines are not started.");
            errors.put( "696", "The state machines are already started.");
            errors.put( "697", "The response looping did not complete.");
            errors.put( "699", "The modem response caused a buffer overflow.");
            errors.put( "700", "The expanded command in the device .INF file is too long.");
            errors.put( "701", "The modem moved to a connection speed not supported by the COM driver.");
            errors.put( "703", "The connection needs information from you, but the application does not allow user interaction.");
            errors.put( "704", "The callback number is invalid.");
            errors.put( "705", "The authorization state is invalid.");
            errors.put( "707", "There was an error related to the X.25 protocol.");
            errors.put( "708", "The account has expired.");
            errors.put( "709", "There was an error changing the password on the domain. The password might have been too short or might have matched a previously used password.");
            errors.put( "710", "Serial overrun errors were detected while communicating with the modem.");
            errors.put( "711", "The Remote Access Service Manager could not start. Additional information is provided in the event log.");
            errors.put( "712", "The two-way port is initializing. Wait a few seconds and redial.");
            errors.put( "713", "No active ISDN lines are available.");
            errors.put( "714", "No ISDN channels are available to make the call.");
            errors.put( "715", "Too many errors occurred because of poor phone line quality.");
            errors.put( "716", "The remote access service IP configuration is unusable.");
            errors.put( "717", "No IP addresses are available in the static pool of remote access service IP addresses.");
            errors.put( "718", "The connection timed out waiting for a valid response from the remote computer.");
            errors.put( "719", "The connection was terminated by the remote computer.");
            errors.put( "721", "The remote computer is not responding.");
            errors.put( "722", "Invalid data was received from the remote computer. This data was ignored.");
            errors.put( "723", "The phone number, including prefix and suffix, is too long.");
            errors.put( "726", "The IPX protocol cannot be used for dial-out on more than one modem at a time.");
            errors.put( "728", "The system cannot find an IP adapter.");
            errors.put( "729", "SLIP cannot be used unless the IP protocol is installed.");
            errors.put( "731", "The protocol is not configured.");
            errors.put( "732", "Your computer and the remote computer could not agree on PPP control protocols.");
            errors.put( "733", "Your computer and the remote computer could not agree on PPP control protocols.");
            errors.put( "734", "The PPP link control protocol was terminated.");
            errors.put( "735", "The requested address was rejected by the server.");
            errors.put( "736", "The remote computer terminated the control protocol.");
            errors.put( "737", "Loopback detected.");
            errors.put( "738", "The server did not assign an address.");
            errors.put( "739", "The authentication protocol required by the remote server cannot use the stored password. Redial, entering the password explicitly.");
            errors.put( "740", "An invalid dialing rule was detected.");
            errors.put( "741", "The local computer does not support the required data encryption type.");
            errors.put( "742", "The remote computer does not support the required data encryption type.");
            errors.put( "743", "The remote server requires data encryption.");
            errors.put( "751", "The callback number contains an invalid character. Only the following characters are allowed: 0 to 9, T, P, W, (,), -, @, and space.");
            errors.put( "752", "A syntax error was encountered while processing a script.");
            errors.put( "753", "The connection could not be disconnected because it was created by the multi-protocol router.");
            errors.put( "754", "The system could not find the multi-link bundle.");
            errors.put( "755", "The system cannot perform automated dial because this entry has a custom dialer specified.");
            errors.put( "756", "This connection is already being dialed.");
            errors.put( "757", "Remote access services could not be started automatically. Additional information is provided in the event log.");
            errors.put( "758", "Internet Connection Sharing is already enabled on the connection.");
            errors.put( "760", "An error occurred while routing capabilities were being enabled.");
            errors.put( "761", "An error occurred while Internet Connection Sharing was being enabled for the connection.");
            errors.put( "763", "Internet Connection Sharing cannot be enabled. There are two or more LAN connections in addition to the connection to be shared.");
            errors.put( "764", "No smart card reader is installed.");
            errors.put( "765", "Internet Connection Sharing cannot be enabled. A LAN connection is already configured with the IP address required for automatic IP addressing.");
            errors.put( "767", "Internet Connection Sharing cannot be enabled. The LAN connection selected on the private network has more than one IP address configured. Reconfigure the LAN connection with a single IP address before enabling Internet Connection Sharing.");
            errors.put( "768", "The connection attempt failed because of failure to encrypt data.");
            errors.put( "769", "The specified destination is not reachable.");
            errors.put( "770", "The remote machine rejected the connection attempt.");
            errors.put( "771", "The connection attempt failed because the network is busy.");
            errors.put( "772", "The remote computer's network hardware is incompatible with the type of call requested.");
            errors.put( "773", "The connection attempt failed because the destination number has changed.");
            errors.put( "774", "The connection attempt failed because of a temporary failure. Try connecting again.");
            errors.put( "775", "The call was blocked by the remote computer.");
            errors.put( "776", "The call could not be connected because the destination has invoked the Do Not Disturb feature.");
            errors.put( "777", "The connection attempt failed because the modem on the remote computer is out of order.");
            errors.put( "778", "It was not possible to verify the identity of the server.");
            errors.put( "780", "An attempted function is not valid for this connection.");
            errors.put( "783", "Internet Connection Sharing cannot be enabled. The LAN connection selected as the private network is either not present, or is disconnected from the network. Please ensure that the LAN adapter is connected before enabling Internet Connection Sharing.");
            errors.put( "784", "You cannot dial using this connection at logon time, because it is configured to use a user name different than the one on the smart card. If you want to use it at logon time, you must configure it to use the user name on the smart card.");
            errors.put( "785", "You cannot dial using this connection at logon time, because it is not configured to use a smart card. If you want to use it at logon time, you must edit the properties of this connection so that it uses a smart card.");
            errors.put( "788", "The L2TP connection attempt failed because the security layer could not negotiate compatible parameters with the remote computer.");
            errors.put( "789", "The L2TP connection attempt failed because the security layer encountered a processing error during initial negotiations with the remote computer.");
            errors.put( "791", "The L2TP connection attempt failed because security policy for the connection was not found.");
            errors.put( "792", "The L2TP connection attempt failed because security negotiation timed out.");
            errors.put( "793", "The L2TP connection attempt failed because an error occurred while negotiating security.");
            errors.put( "794", "The Framed Protocol RADIUS attribute for this user is not PPP.");
            errors.put( "795", "The Tunnel Type RADIUS attribute for this user is not correct.");
            errors.put( "796", "The Service Type RADIUS attribute for this user is neither Framed nor Callback Framed.");
            errors.put( "797", "A connection to the remote computer could not be established because the modem was not found or was busy.");
            errors.put( "799", "Internet Connection Sharing (ICS) cannot be enabled due to an IP address conflict on the network. ICS requires the host be configured to use 192.168.0.1. Please ensure that no other client on the network is configured to use 192.168.0.1.");
            errors.put( "800", "Unable to establish the VPN connection. The VPN server may be unreachable, or security parameters may not be configured properly for this connection.");
            
        }
        
        String errorAsString = "" + errorCode;
        if( errors.containsKey(errorAsString) ) 
            return errorAsString + " (" + errors.get(errorAsString) + ")"; 
        else 
            return errorAsString + " (" + "unknown error code" + ")";
        
    }
    
    
}
package com.energyict.echelon;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.FolderVersionShadow;
import com.energyict.xml.xmlhelper.DomHelper;

import java.sql.SQLException;
import java.util.*;

import org.w3c.dom.*;

public class NesCreateMeterAction implements FolderAction {

    private String trafo;
    private String timezone;

    public void execute(final Folder folder) throws SQLException, BusinessException {
        
        try {
            
            String param = buildParam(folder);
            // TODO: replace serverUri !!
            String sessionId = EchelonSession.getInstance("10.0.0.102").getSession();
            Document d = EchelonSession.getInstance("10.0.0.102").createDevice(param, sessionId);
            
            Element id = Util.xPath(d, "//DEVICEID" );
            
            FolderVersionShadow fvs = folder.getLastVersion().getShadow();
            fvs.setFrom(new Date() );
            fvs.set( "nesId", id.getFirstChild().getNodeValue());
            
            folder.createVersion(fvs);
            
        } catch(Exception ex){
            throw new BusinessException(ex);
        }

    }

    private String buildParam(final Folder folder) throws EchelonException {

        // TODO: replace serverUri !!
        EchelonSession session = EchelonSession.getInstance("10.0.0.102");
        
        String sessionId = EchelonSession.getInstance("10.0.0.102").getSession();
        final String timezone = session.getTimezone(getTimezone(folder), sessionId);
        final String serial = (String)folder.get("serialNumber");
        final String gatewayId = (String)folder.getParent().get( "nesId" );
        final String neuronId = (String)folder.get("neuronId");
        final String provisioningToolKey = (String)folder.get("provisioningtoolKey");
        final String authentication = (String)folder.get("authenticationKey");
        
        DomHelper dom = new DomHelper( "PARAMETERS" ) {{
            
            addElement("NAME",          serial );
            addElement("TYPEID",        Constants.DeviceTypes.METER );
            addElement("GATEWAYID",     gatewayId );
            addElement("SERIALNUMBER",  serial );
            addElement("TIMEZONEID",    timezone );
   
            Element mtr = addElement("METER");
            
            addElement(mtr, "NEURONID",            neuronId);
            addElement(mtr, "TRANSFORMERID",       getTrafo(folder) );
            addElement(mtr, "PROVISIONINGTOOLKEY", provisioningToolKey );
            addElement(mtr, "AUTHENTICATIONKEY",   authentication );
            
        }};
        
        return Util.scrubHeader(dom.toXmlString());
    }

    public boolean isEnabled(Folder folder) {
        return (getTrafo(folder) != null ) && (folder.get("nesId")==null );
        
    }

    /* take trafo property from concentrator unless overridden by meter*/
    private String getTrafo(Folder folder) {
        if( trafo == null ) {
            trafo = (String)folder.get( "transformer" );
            if( trafo == null ) {
                trafo = (String)folder.getParent().get( "transformer" );
            }
        } 
        return trafo;
    }
    
    /* take timezone property from concentrator unless overridden by meter*/
    private String getTimezone(Folder folder) {
        if( timezone == null ) {
            timezone = (String)folder.get( "timezone" );
            if( timezone == null ) {
                timezone = (String)folder.getParent().get( "timezone" );
            }
        } 
        return timezone;
    }

    public void addProperties(Properties properties) { }

    public List getOptionalKeys() {
        return new ArrayList();
    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

    public String getVersion() {
        return "$Revision: 1.5 $";      
    }
    
}

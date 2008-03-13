package com.energyict.echelon;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.FolderVersionShadow;
import com.energyict.xml.xmlhelper.DomHelper;

import java.sql.SQLException;
import java.util.*;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;

public class NesCreateConcentratorAction implements FolderAction {

    public void execute(Folder f) throws SQLException, BusinessException {
        
        try {
        
            final String name = f.getName();
            final String description = "";
            final String gatewayTypeId = Constants.GatewayTypes.DC1000;
            final String statusTypeId = Constants.GatewayStatus.ENABLED;
            
            final Date installationDateTime = new Date();
            
            String tz = (String)f.get("timezone");
            // TODO: replace serverUri !!
            String sessionId = EchelonSession.getInstance("10.0.0.102").getSession();
            final String timezone = EchelonSession.getInstance("10.0.0.102").getTimezone( tz, sessionId ); 
            final String hardwareVersion = "";
            final String serialNumber = (String)f.get("serialNumber");
            final String ipAddress = (String)f.get("ipAddress");
    
            final String pppLogin = (String)f.get("uniqueKeyUsername");
            final String pppPassword = (String)f.get("uniqueKeyPassword");
            final String typeId = Constants.GatewayCommunicationTypes.ALWAYS_ON_IP;
            
            final String neuronId = (String)f.get("neuronId");
            final String transformerId = (String)f.get("transformer");
            final String programId = "";
            final String authentication = Constants.StandardAPIOptions.NO;
            
            
            DomHelper builder = 
                
                new DomHelper("PARAMETERS") {{
            
                addElement("NAME",          name);
                addElement("DESCRIPTION",   description);
                addElement("TYPEID",        gatewayTypeId);
                addElement("STATUSTYPEID",  statusTypeId);
                
                String date = Util.DATE_FORMAT.format(installationDateTime);
                addElement("INSTALLATIONDATETIME", date);
                addElement("TIMEZONEID",           timezone);
                addElement("HARDWAREVERSION",      hardwareVersion);
                
                addElement("SERIALNUMBER",  serialNumber);
                addElement("IPADDRESS",     ipAddress);
                
                Element e = addElement("SERVERTOGATEWAYCOMMUNICATION");
                    
                addElement(e, "PPPPASSWORD",  pppPassword);
                addElement(e, "PPPLOGIN",     pppLogin);
                addElement(e, "TYPEID",       typeId);
            
                e = addElement("DC1000");
                
                addElement(e, "NEURONID",         neuronId);
                addElement(e, "TRANSFORMERID",    transformerId);
                addElement(e, "PROGRAMID",        programId);
                addElement(e, "APPLICATIONLEVELAUTHENTICATION", authentication);
                
            }};
            
            String param = Util.scrubHeader(builder.toXmlString());
            // TODO: replace serverUri !!
            Document result = EchelonSession.getInstance("10.0.0.102").register(param, sessionId);
            
            String xPath = "//GATEWAYID";
            Element e = Util.xPath(result, xPath);
            
            if( e==null ) {
                String msg = "Unexpected result, did not get a NES ID back";
                throw new BusinessException( msg );
            }
            
            String id = e.getFirstChild().getNodeValue();
            
            FolderVersionShadow fvs = f.getLastVersion().getShadow();
            fvs.setFrom(new Date());
            fvs.set("nesId", id);

            f.createVersion(fvs);
            
        } catch (EchelonException e) {
            throw new BusinessException(e);
        } catch (TransformerException e) {
            throw new BusinessException(e);
        } catch (FactoryConfigurationError e) {
            throw new BusinessException(e);
        }
        
    }

    public boolean isEnabled(Folder folder) {
        
        if( isEmpty( folder.getName() ) )                           return false;
        if( isEmpty( (String)folder.get("serialNumber") ) )         return false;
//        if( isEmpty( (String)folder.get("ipAddress") ) )            return false;

        if( isEmpty( (String)folder.get("uniqueKeyUsername") ) )    return false;
        if( isEmpty( (String)folder.get("uniqueKeyPassword") ) )    return false;
        
//        if( isEmpty( (String)folder.get("transformer") ) )          return false;
        if( isEmpty( (String)folder.get("neuronId") ) )             return false;
        
//        if( isEmpty( (String)folder.get("timezone") ) )             return false;
        
        /* already created in Nes */
        if( folder.get( "nesId" ) != null )                         return false;
        
        return true;
        
    }
    
    private boolean isEmpty(String variable) {
        return variable == null || "".equals(variable);
    }

    public void addProperties(Properties properties) { }

    public List getOptionalKeys() {
        return new ArrayList();
    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

    public String getVersion() {
        return "$Revision: 1.6 $";      
    }
    
    public static void main(String[]args) throws Exception, BusinessException{
        
        MeteringWarehouse mw = new MeteringWarehouseFactory( ).getBatch();
        Folder f = mw.getFolderFactory().find(6163);
        new NesCreateConcentratorAction().execute(f);
        
    }
 
}

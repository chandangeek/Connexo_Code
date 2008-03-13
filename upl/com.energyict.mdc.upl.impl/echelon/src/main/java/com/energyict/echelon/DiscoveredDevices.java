package com.energyict.echelon;

import com.energyict.cbo.*;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.*;
import com.energyict.mdwswing.util.UiHelper;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.*;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;

public class DiscoveredDevices implements FolderAction, Transaction {
    
    
    private Folder folder = null;
    
    public void execute(Folder folder) throws SQLException, BusinessException {
        this.folder = folder;
        doExecute();
    }

    
    public Object doExecute() throws BusinessException, SQLException {
        if( folder == null ) { 
            throw new ApplicationException( "" );
        }
        
        try {

            String param = 
                 "<PARAMETERS>" 
//
//                +   "<RESULTTYPES>" 
//                +     "<RESULTTYPE>" 
//                +       "<ID>ad2bbabf67e549ec8de11e31592313ba</ID>" 
//                +     "</RESULTTYPE>"
//                +   "</RESULTTYPES>"
                
               +   "<GATEWAYS>" 
               +     "<GATEWAY>" 
               +       "<ID>801957343ad548e1bafa1c69e319499c</ID>" 
               +     "</GATEWAY>"
               +   "</GATEWAYS>"
               
               + "</PARAMETERS>";

            
            // TODO: replace serverUri !!
            String sessionId = EchelonSession.getInstance("10.0.0.102").getSession();
            Document d = EchelonSession.getInstance("10.0.0.102").retrieveResultList(param, sessionId);

            NodeList list = Util.xPathNodeList(d, "RETURNS/APIPAYLOAD/RESULTS/RESULT");    
            for( int i = 0; i < list.getLength(); i++ ){
                System.out.println( Util.getNodeValue((Element)list.item(i), "ID"));
            }
            
          //  System.out.println(result);
            d = EchelonSession.getInstance("10.0.0.102").retrieveResult("e9c72e50c72741a5b49daaa990a1a0c2", sessionId);
        
            
            DeviceMasterDetailPanel panel = new DeviceMasterDetailPanel(d);
            Dimension dim = new Dimension(300, 500);
            String title = "Discovered meters";
            UiHelper.getMainWindow().showModalDialog(panel, title, dim);

        } catch (EchelonException e) {
            e.printStackTrace();
            throw new BusinessException(e);
        } catch (FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        
        return null;
    }
    
    
    public boolean isEnabled(Folder folder) {
        return true;
    }
    
    public void addProperties(Properties properties) {}
    
    public List getOptionalKeys() {
        return new ArrayList();
    }
    
    public List getRequiredKeys() {
        return new ArrayList();
    }
    
    public String getVersion() {
        return "$Revision: 1.7 $";
    }
    
    public static void main( String [] args )throws Exception {
        new DiscoveredDevices().execute(null);
    }



}

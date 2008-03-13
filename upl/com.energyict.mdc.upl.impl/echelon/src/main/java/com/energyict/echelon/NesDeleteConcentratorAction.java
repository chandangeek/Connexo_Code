package com.energyict.echelon;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;
import com.energyict.xml.xmlhelper.DomHelper;

import java.sql.SQLException;
import java.util.*;

import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;

/* Delete all results - needs a lot of work */

public class NesDeleteConcentratorAction implements FolderAction {
    
    public void execute(Folder folder) throws SQLException, BusinessException {

        try {
            
            String param = buildParams(folder);
            // TODO: replace serverUri !!
            String sessionId = EchelonSession.getInstance("10.0.0.102").getSession();
            Document d = EchelonSession.getInstance("10.0.0.102").retrieveResultList(param, sessionId);
            
            String xPath = "//RETURNS/APIPAYLOAD/RESULTS/RESULT/ID";
            NodeIterator i = Util.xPathNodeIterator(d, xPath);
            Node node = (Node)i.nextNode();
            
            DomHelper domHelper = new DomHelper( "PARAMETERS" );
            Element results = domHelper.addElement("RESULTS");
            
            while (node != null) {
             
                String id = null;
                id = node.getFirstChild().getNodeValue();
                Element result =  domHelper.addElement(results, "RESULT");
                domHelper.addElement(result, "ID", id);
                node = i.nextNode();
                
            }
            
            param = Util.scrubHeader( domHelper.toXmlString() );
            // TODO: replace serverUri !!
            EchelonSession.getInstance("10.0.0.102").deleteResultList( param, sessionId );
            
        } catch (EchelonException ee) {
            throw new BusinessException(ee);
        } catch (TransformerException te) {
            throw new BusinessException(te);
        }
        
    }

    
    private String buildParams(Folder folder) {
        String nesId = (String)folder.get( "nesId" );
        
        DomHelper dom = new DomHelper( "PARAMETERS" );
        Element devices = dom.addElement( "DEVICES" );
        Element device = dom.addElement( devices, "DEVICE" );
        
        dom.addElement( device, "ID", nesId );
        return dom.toXmlString();
    }
    
    public boolean isEnabled(Folder folder) {
        return folder.get( "nesId" ) != null;
    }

    public void addProperties(Properties properties) { }

    public List getOptionalKeys() {
        return new ArrayList();
    }

    public List getRequiredKeys() {
        return new ArrayList();
    }

    public String getVersion() {
        return "$Revision: 1.4 $";
    }

}

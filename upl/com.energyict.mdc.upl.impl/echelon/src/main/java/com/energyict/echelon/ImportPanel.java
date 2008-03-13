package com.energyict.echelon;

import com.energyict.cbo.*;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.*;
import com.energyict.mdwswing.core.EisPropsPnl;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ImportPanel extends EisPropsPnl implements Transaction {

    private JTextField fileNameTextField;
    
    private JLabel errorLabel;
    private JButton okButton;
    
    private JComboBox meterCombo;
    private JComboBox rtuCombo;
    private JComboBox concentratorCombo;
    
    private DefaultComboBoxModel folderTypeModel;
    private DefaultComboBoxModel rtuTypeModel;
    
    private File choosenFile;
    
    private final ActionListener browseAction = new BrowseActionListener();
    private final ActionListener okAction = new OkActionListener();
    private final ActionListener cancelAction = new CancelActionListener();

    private final Folder parent;

    private static final String IMPORT_FILE_ERROR = 
        "File does not contain meters or concentrators";
    
    public ImportPanel(Folder parent, final boolean allowConcentrator){
        
        this.parent = parent;
        setLayout( new GridBagLayout() );
        
        add(new JLabel( "File:" ), GbUtil.gbc(0, 0));
        add(fileNameTextField = new JTextField() {{ 
                setEnabled(false);
            }}
            , GbUtil.gbc(1, 1, 0) );
        add(new JButton( "..." ){{ 
            addActionListener(browseAction);
        }} );
        
        
        add(
            new JPanel(){{
                setLayout(new GridBagLayout());
            
                add(new JLabel("Meter Folder type:"), GbUtil.gbc(0, 0));
                add( meterCombo = 
                        new JComboBox() {{ 
                            setModel(getFolderTypeModel());
                            setEnabled(false);
                        }}, GbUtil.gbc(1, 1, 0));
                
                add(new JLabel("Meter Rtu type:"), GbUtil.gbc(0, 1));
                add( rtuCombo = 
                    new JComboBox() {{ 
                        setModel(getRtuTypeModel());
                        setEnabled(false);
                    }}, GbUtil.gbc(1, 1, 1));
                
                if( allowConcentrator ){
                    add(new JLabel("Concentrator folder type:"), GbUtil.gbc(0, 2));
                    add(concentratorCombo = 
                            new JComboBox() {{
                                setModel(getFolderTypeModel());
                                setEnabled(false);
                            }}, GbUtil.gbc(1, 1, 2));
                }    
                setBorder(new TitledBorder("Folder Type"));
                
            }}, GbUtil.gbc(1, 0, 1, 3));
        
        add( 
            new JPanel( ){{
                setLayout(new BorderLayout());
                add( errorLabel = new JLabel( IMPORT_FILE_ERROR ){{
                    setForeground(Color.RED);
                    setVisible(false);
                }} );
                add(
                    new JPanel( ){{
                        setLayout(new GridLayout(1, 0, 6, 0));
                        add(okButton = 
                                new JButton("OK"){{
                                    setEnabled(false);
                                    addActionListener(okAction);
                                }});
                        add(
                            new JButton("Cancel"){{
                                addActionListener(cancelAction);
                        }});
                    }}, BorderLayout.EAST);
                }}, GbUtil.gbc(0,0,2,3));
            
    }
    
    class BrowseActionListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(ImportPanel.this);
            
            if(returnVal != JFileChooser.APPROVE_OPTION) return;
            
            try {
                
                File f = chooser.getSelectedFile();
                
                if( !isFileMorphologicallyOk(f) ) 
                    return;
                
                fileNameTextField.setText(f.getAbsolutePath());
                
                NodeList meterList = toMeterList(f);
                NodeList concentratorList = toConcentratorList(f);
                
                boolean hasMeters = meterList.getLength() > 0;
                boolean hasConentrators = concentratorList.getLength() > 0;
                
                if( meterCombo != null )  
                    meterCombo.setEnabled(hasMeters);
                if( rtuCombo != null )    
                    rtuCombo.setEnabled(hasMeters);
                if( concentratorCombo != null ) 
                    concentratorCombo.setEnabled(hasConentrators);
                
                okButton.setEnabled(hasMeters || hasConentrators);
                
                choosenFile = f;
                flagError(false);
                
            } catch (ParserConfigurationException e1) {
                e1.printStackTrace();
                return;
            } catch (SAXException e1) {
                e1.printStackTrace();
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            } catch (TransformerException e1) {
                e1.printStackTrace();
                return;
            }
                
            
        }

        /* check the external appearance */
        private boolean isFileMorphologicallyOk(File file) {
            
            if( !file.exists() ) {                   
                flagError(true);
                return false;
            }
            
            if( !file.isFile() ) { 
                flagError(true);
                return false;
            }
            
            if( !file.canRead() ) {
                flagError(true);
                return false;
            }
            
            if( file.getName().endsWith("lnk" ) ){
                flagError(true);
                return false;
            }
            
            return true;
            
        }
    
    }
    
    private void flagError(boolean flag) {
        errorLabel.setVisible(flag);
    }
    
    class OkActionListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            
            if(choosenFile==null) 
                throw new ApplicationException( "chooseFile is null" );
            
            if(parent==null) 
                throw new ApplicationException( "parent is null" );
            
            try {
                doExecute();
            } catch (BusinessException be) {
                throw new ApplicationException(be);
            } catch (SQLException ae) {
                throw new ApplicationException(ae);
            }
            
        }
        
    }

    public Object doExecute() throws BusinessException, SQLException {
        
        try {
            
            NodeList meterList = toMeterList(choosenFile);
            NodeList concentratorList = toConcentratorList(choosenFile);
        
            for(int i=0; i<meterList.getLength(); i++){
                meterToTypedFolder( (Element)meterList.item(i) );
            }
            
            for(int i=0; i<concentratorList.getLength(); i++){
                concentratorToTypedFolder( (Element)concentratorList.item(i) );
            }
            
            doClose();
            
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
            throw new ApplicationException(e1);
        } catch (SAXException e1) {
            e1.printStackTrace();
            throw new ApplicationException(e1);
        } catch (IOException e1) {
            e1.printStackTrace();
            throw new ApplicationException(e1);
        } catch (TransformerException e1) {
            e1.printStackTrace();
            throw new ApplicationException(e1);
        }
        
        return null;
        
    }
    
    class CancelActionListener implements ActionListener {
        
        public void actionPerformed(ActionEvent e) {
            doClose();
        }
        
    }
    
    public DefaultComboBoxModel getFolderTypeModel() {
        if(folderTypeModel==null){
            
            ArrayList shadows = new ArrayList();
            
            MeteringWarehouse mw = MeteringWarehouse.getCurrent();
            Iterator i = mw.getFolderTypeFactory().findAll().iterator();
            while(i.hasNext()){
                FolderType ft = (FolderType)i.next();
                shadows.add( new FolderTypeWrapper( ft.getShadow() ) );
            }
            
            Object [] ar = shadows.toArray(new Object[0]);
            folderTypeModel = new DefaultComboBoxModel(ar);
            
        }
        return folderTypeModel;
    }
    
    private class FolderTypeWrapper {
        
        FolderTypeShadow shadow;
        
        FolderTypeWrapper(FolderTypeShadow shadow){
            this.shadow = shadow;
        }
        
        FolderTypeShadow getShadow( ){
            return shadow;
        }
        
        public String toString( ){
            return shadow.getName();
        }
        
    }
    
    public DefaultComboBoxModel getRtuTypeModel() {
        if(rtuTypeModel==null){
            ArrayList shadows = new ArrayList();
            
            MeteringWarehouse mw = MeteringWarehouse.getCurrent();
            Iterator i = mw.getRtuTypeFactory().findAll().iterator();
            while(i.hasNext()){
                RtuType ft = (RtuType)i.next();
                shadows.add( new RtuTypeWrapper( ft.getShadow() ) );
            }
            
            Object [] ar = shadows.toArray(new Object[0]);
            rtuTypeModel = new DefaultComboBoxModel(ar);
            
        }
        return rtuTypeModel;
    }
    
    private class RtuTypeWrapper {
        
        RtuTypeShadow shadow;
        
        RtuTypeWrapper(RtuTypeShadow shadow){
            this.shadow = shadow;
        }
        
        RtuTypeShadow getShadow( ){
            return shadow;
        }
        
        public String toString( ){
            return shadow.getName();
        }
        
    }
    
    private NodeList toMeterList(File f) 
        throws ParserConfigurationException, SAXException, IOException, 
               TransformerException{
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document d = builder.parse(f);
        
        String xPath = "/ECHELONMETERLIST/METERLIST/METER";
        return Util.xPathNodeList(d, xPath);
        
    }
    
    private NodeList toConcentratorList(File f) 
        throws ParserConfigurationException, SAXException, IOException, 
               TransformerException{
    
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document d = builder.parse(f);
        
        String xPath = 
            "/ECHELONDATACONCENTRATORLIST/DATACONCENTRATORLIST/DATACONCENTRATOR";
        
        return Util.xPathNodeList(d, xPath);
        
    }    
    
    private void meterToTypedFolder( Element meter) 
        throws BusinessException, SQLException {
        
        String neuronId             = Util.getNodeValue(meter, "NEURONID");
        String provisioningKey      = Util.getNodeValue(meter, "PROVISIONINGKEY");
        String provisioningToolKey  = Util.getNodeValue(meter, "PROVISIONINGTOOLKEY");
        String uniqueKey            = Util.getNodeValue(meter, "UNIQUEKEY");
        String readOnlyKey          = Util.getNodeValue(meter, "READONLYKEY");
        String lontalkKey           = Util.getNodeValue(meter, "LONTALKKEY");
        String authenticationKey    = Util.getNodeValue(meter, "AUTHENTICATIONKEY");
        String serialNumber         = Util.getNodeValue(meter, "SERIALNUMBER");
        
        FolderShadow shadow = new FolderShadow();
        shadow.setName(serialNumber);
        shadow.setExternalName(serialNumber);
        shadow.setFolderTypeId(getFolderType().getId());
        Folder child = parent.createFolder(shadow);

        FolderVersionShadow fvShadow = new FolderVersionShadow();
        fvShadow.setFrom(new Date());
        
        fvShadow.set( "neuronId", neuronId);
        fvShadow.set( "provisioningKey", provisioningKey);
        fvShadow.set( "provisioningtoolKey", provisioningToolKey);
        fvShadow.set( "uniqueKey", uniqueKey);
        fvShadow.set( "readOnlyKey", readOnlyKey);
        fvShadow.set( "lontalkKey", lontalkKey);
        fvShadow.set( "authenticationKey", authenticationKey);
        fvShadow.set( "serialNumber", serialNumber);
        
        child.createVersion(fvShadow);
        
        RtuShadow rtuShadow = new RtuShadow();
        rtuShadow.setRtuTypeId( getRtuType( ).getId() );
        rtuShadow.setName( serialNumber );
        rtuShadow.setSerialNumber( serialNumber );
        rtuShadow.setIntervalInSeconds( 3600 );
        rtuShadow.setTimeZone( TimeZone.getDefault() );
        rtuShadow.setFolderId( child.getId() );
        
        child.createRtu(rtuShadow);
        
    }

    private void concentratorToTypedFolder(Element concent) 
        throws BusinessException, SQLException {
        
        String neuronId             
                = Util.getNodeValue(concent, "NEURONID");
        String provisioningKeyUsername      
                = Util.getNodeValue(concent, "PROVISIONINGKEYUSERNAME");
        String provisioningKeyPassword  
                = Util.getNodeValue(concent, "PROVISIONINGKEYPASSWORD");
        String uniqueKeyUsername            
                = Util.getNodeValue(concent, "UNIQUEKEYUSERNAME");
        String uniqueKeyPassword          
                = Util.getNodeValue(concent, "UNIQUEKEYPASSWORD");
        String login           
                = Util.getNodeValue(concent, "LOGIN");
        String password    
                = Util.getNodeValue(concent, "PASSWORD");
        String serialNumber         
                = Util.getNodeValue(concent, "SERIALNUMBER");
        
        FolderShadow shadow = new FolderShadow();
        shadow.setName(serialNumber);
        shadow.setExternalName(serialNumber);
        shadow.setFolderTypeId(getFolderType().getId());
        
        Folder child = parent.createFolder(shadow);

        FolderVersionShadow fvShadow = new FolderVersionShadow();
        fvShadow.setFrom(new Date());
        
        fvShadow.set( "neuronId", neuronId);
        fvShadow.set( "provisioningKeyUsername", provisioningKeyUsername);
        fvShadow.set( "provisioningKeyPassword", provisioningKeyPassword);
        fvShadow.set( "uniqueKeyUsername", uniqueKeyUsername);
        fvShadow.set( "uniqueKeyPassword", uniqueKeyPassword);
        fvShadow.set( "login", login);
        fvShadow.set( "password", password);
        fvShadow.set( "serialNumber", serialNumber);
        
        child.createVersion(fvShadow);
        
    }
    
    
    private FolderTypeShadow getFolderType( ){
        return ((FolderTypeWrapper)folderTypeModel.getSelectedItem()).getShadow();
    }

    private RtuTypeShadow getRtuType( ){
        return ((RtuTypeWrapper)rtuTypeModel.getSelectedItem()).getShadow();
    }

    
    public static void main(String args[]){
    
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add( new ImportPanel(null, false) );
        
    }
    
    
}

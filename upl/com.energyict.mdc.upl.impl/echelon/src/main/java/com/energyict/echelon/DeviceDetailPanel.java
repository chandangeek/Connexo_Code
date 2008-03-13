package com.energyict.echelon;

import java.awt.GridBagLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.w3c.dom.*;

class DeviceDetailPanel extends JPanel {
    
    private JLabel id;
    private JLabel serialNumber;
    private JLabel neuronId;
    
    private JLabel bestGatewayId;
    private JLabel bestGatewaySerialNumber;
    private JLabel bestGatewayNeuronId;

    private JLabel repeaterId;
    private JLabel repeaterSerialNumber;
    private JLabel repeaterNeuronId;
    
    private JLabel repeaterCount;
    private JLabel repeaterLastContact;
    private JLabel repeaterPrimaryFrequency;
    
    public DeviceDetailPanel() {
        init();
    }
    
    void init( ){
        setLayout( new GridBagLayout() );
        
        add( new JPanel( ) {{  
            setLayout( new GridBagLayout() );        
        
            add( new JLabel("Id:"), GbUtil.gbc(0, 0) );
            add( id = new JLabel(), GbUtil.gbc(1, 1, 0) );
            add( new JLabel("Serialnumber:"), GbUtil.gbc(0, 1) );
            add( serialNumber = new JLabel(), GbUtil.gbc(1, 1, 1) );
            add( new JLabel("NeuronId:"), GbUtil.gbc(0, 2) );
            add( neuronId = new JLabel(), GbUtil.gbc(1, 1, 2) );
            setBorder(new TitledBorder("Meter"));
            
        }}, GbUtil.gbc(1, 0, 0, 2)  );
        
        add( new JPanel( ) {{  
            setLayout( new GridBagLayout() );

            add( new JLabel("Id:"), GbUtil.gbc(0, 0) );
            add( bestGatewayId = new JLabel(), GbUtil.gbc(1, 1, 0) );
            add( new JLabel("Serialnumber:"), GbUtil.gbc(0, 1) );
            add( bestGatewaySerialNumber = new JLabel(), GbUtil.gbc(1, 1, 1) );
            add( new JLabel("NeuronId:"), GbUtil.gbc(0, 2) );
            add( bestGatewayNeuronId = new JLabel(), GbUtil.gbc(1, 1, 2) );
            
            setBorder(new TitledBorder("Best gateway"));
            
        }}, GbUtil.gbc(1, 0, 3, 2)  );

        add( new JPanel( ) {{  
            setLayout( new GridBagLayout() );

            add( new JLabel("Id:"), GbUtil.gbc(0, 0) );
            add( repeaterId = new JLabel(), GbUtil.gbc(1, 1, 0) );
            add( new JLabel("Serialnumber:"), GbUtil.gbc(0, 1) );
            add( repeaterSerialNumber = new JLabel(), GbUtil.gbc(1, 1, 1) );
            add( new JLabel("NeuronId:"), GbUtil.gbc(0, 2) );
            add( repeaterNeuronId = new JLabel(), GbUtil.gbc(1, 1, 2) );
            
            add( new JLabel("Count:"), GbUtil.gbc(0, 3) );
            add( repeaterCount = new JLabel(), GbUtil.gbc(1, 1, 3) );
            add( new JLabel("Last contact:"), GbUtil.gbc(0, 4) );
            add( repeaterLastContact = new JLabel(), GbUtil.gbc(1, 1, 4) );
            add( new JLabel("Primary frequency:"), GbUtil.gbc(0, 5) );
            add( repeaterPrimaryFrequency = new JLabel(), GbUtil.gbc(1, 1, 5) );
            
            setBorder(new TitledBorder("Repeater"));
            
        }}, GbUtil.gbc(1, 0, 4, 2)  );
        
        set(null);
    }
    
    void set(Element node){
        
        id.setText( Util.getNodeValue(node, "ID") );
        serialNumber.setText( Util.getNodeValue(node, "SERIALNUMBER") );
        neuronId.setText( Util.getNodeValue(node, "NEURONID") );

        Element gw = (Element) Util.firstNode(node, "BESTGATEWAY");
        
        bestGatewayId.setText( Util.getNodeValue(gw, "ID") );
        bestGatewaySerialNumber.setText( Util.getNodeValue(gw, "SERIALNUMBER") );
        bestGatewayNeuronId.setText( Util.getNodeValue(gw, "NEURONID") );
        
        
        Element repeat = (Element) Util.firstNode(node, "REPEATER");
        
        repeaterId.setText( Util.getNodeValue(repeat, "ID") );
        repeaterSerialNumber.setText( Util.getNodeValue(repeat, "SERIALNUMBER") );
        repeaterNeuronId.setText( Util.getNodeValue(repeat, "NEURONID") );
        
        repeaterCount.setText( Util.getNodeValue(repeat, "COUNT") );
        repeaterLastContact.setText( Util.getNodeValue(repeat, "LASTCONTACTDATETIME") );
        repeaterPrimaryFrequency.setText( Util.getNodeValue(repeat, "USESPRIMARYFREQUENCY") );
    }
    
    public static void main( String [] args ){
        
        JFrame f = new JFrame();
        f.getContentPane().add( new DeviceDetailPanel() );
        f.show();
    }
    
}

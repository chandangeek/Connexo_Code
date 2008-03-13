package com.energyict.echelon;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;
import javax.xml.transform.TransformerException;

import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;

class DeviceMasterDetailPanel extends JPanel {
    
    private Document document;
    
    private JTable table;
    private DeviceTableModel tableModel;
    private DeviceDetailPanel detailPanel;
    
    private ListSelectionListener listener = new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent e) {
            if( !e.getValueIsAdjusting() ){
                Node n = tableModel.getNode( table.getSelectedRow() );
                System.out.println( n );
                
                detailPanel.set((Element)n);
            }
        }
    };
    
    public DeviceMasterDetailPanel(Document document) throws TransformerException {
        this.document = document;
        init();
    }
    
    void init( ) throws TransformerException{
        setLayout(new BorderLayout());
        
        tableModel=new DeviceTableModel();
        String xPath = "/RETURNS/APIPAYLOAD/RESULT/DATA/DEVICES/DEVICE";
        tableModel.nodeList = Util.xPathNodeList(document, xPath);
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(listener);
        add( new JScrollPane(table), BorderLayout.CENTER);
        
        add( 
        new JPanel() {{
           setLayout(new BorderLayout());
           add( detailPanel = new DeviceDetailPanel( ), BorderLayout.CENTER );
           
        }}, BorderLayout.SOUTH );
           
        //add( detailPanel = new DeviceDetailPanel(), BorderLayout.SOUTH );
    }
    
    class DeviceTableModel implements TableModel  {
        
        NodeList nodeList;
        
        Node getNode(int idx){
            return nodeList.item(idx);
        }
        
        public Class getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getColumnCount() {
            if( nodeList == null ) return 0;
            return nodeList.getLength();
        }

        public String getColumnName(int columnIndex) {
            if( columnIndex == 0 )
                return "Serial number";
            if( columnIndex == 1 )
                return "Neuron id";
            return "<unknown>";
        }

        public int getRowCount() {
            return nodeList.getLength();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Node n = nodeList.item(rowIndex);
            if(columnIndex == 0) 
                return Util.getNodeValue((Element)n, "SERIALNUMBER");
            if(columnIndex == 1) 
                return Util.getNodeValue((Element)n, "NEURONID");
            return "";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        
        public void addTableModelListener(TableModelListener l) { }
        public void removeTableModelListener(TableModelListener l) {}
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
        
    }
    
    public static void main( String [] args ) throws Exception{
        
        String xml = 
            "<RETURNS>" +
            "  <STATUS>A92A7EBEE897499fA8B06D5FE94B8A30</STATUS>" +
            "   <APIPAYLOAD>" +
            "       <RESULT>" +
            "           <ID>12f7a14b7cc845fe895416ea63c07a1f</ID>" +
            "           <GATEWAYID>a9997f8f65514570b7415753d4fe9f83</GATEWAYID>" +
            "           <DATETIME>2007-04-24 09:45:44.940</DATETIME>" +
            "           <COMMANDHISTORYID>12f7a14b7cc845fe895416ea63c07a1f</COMMANDHISTORYID>" +
            "           <TYPEID>ad2bbabf67e549ec8de11e31592313ba</TYPEID>" +
            "           <GATEWAY>" +
            "               <ID>a9997f8f65514570b7415753d4fe9f83</ID>" +
            "               <SOFTWAREVERSION>2.10</SOFTWAREVERSION>" +
            "               <TYPEID>7428ddbc573941f683c28212f8a0a746</TYPEID>" +
            "           </GATEWAY>" +
            "           <DATA>" +
            "               <DEVICES>" +
            "                   <DEVICE>" +
            "                       <ID>bd8c39705402434591190afd9173d7f7</ID>" +
            "                       <SERIALNUMBER>LW00074163</SERIALNUMBER>" +
            "                       <NEURONID>050138305A00</NEURONID>" +
            "                       <REPEATER>" +
            "                           <TYPE>8eb085070912454295b35e0740eddd96</TYPE>" +
            "                           <ID></ID>" +
            "                           <NEURONID>050138320700</NEURONID>" +
            "                           <SERIALNUMBER>LW00076683</SERIALNUMBER>" +
            "                           <SIGNALQUALITY>" +
            "                               <CARRIERMARGIN>27</CARRIERMARGIN>" +
            "                               <RECEIVESIGNALSTRENGTH>-36</RECEIVESIGNALSTRENGTH>" +
            "                           </SIGNALQUALITY>" +
            "                           <COUNT>1</COUNT>" +
            "                           <LASTCONTACTDATETIME>2007-04-24 05:28:05.000</LASTCONTACTDATETIME>" +
            "                           <USESPRIMARYFREQUENCY>0</USESPRIMARYFREQUENCY>" +
            "                       </REPEATER>" +
            "                       <DISCOVEREDTYPEID>f6676a4ae64d475f9a3153da9d847027</DISCOVEREDTYPEID>" +
            "                       <BESTGATEWAY>" +
            "                           <ID>a9997f8f65514570b7415753d4fe9f83</ID>" +
            "                           <NEURONID>0458243E0100</NEURONID>" +
            "                           <SERIALNUMBER>LW00023525</SERIALNUMBER>" +
            "                       </BESTGATEWAY>" +
            "                   </DEVICE>" +
            "                   <DEVICE>" +
            "                       <ID></ID>" +
            "                       <SERIALNUMBER>LW00076683</SERIALNUMBER>" +
            "                       <NEURONID>050138320700</NEURONID>" +
            "                       <REPEATER>" +
            "                           <TYPE>2851897aec124b189e91b136aec679c2</TYPE>" +
            "                           <ID>a9997f8f65514570b7415753d4fe9f83</ID>" +
            "                           <NEURONID>0458243E0100</NEURONID>" +
            "                           <SERIALNUMBER>LW00023525</SERIALNUMBER>" +
            "                           <SIGNALQUALITY>" +
            "                               <CARRIERMARGIN>36</CARRIERMARGIN>" +
            "                               <RECEIVESIGNALSTRENGTH>0</RECEIVESIGNALSTRENGTH>" +
            "                           </SIGNALQUALITY>" +
            "                           <COUNT>0</COUNT>" +
            "                           <LASTCONTACTDATETIME>2007-04-24 09:38:18.000</LASTCONTACTDATETIME>" +
            "                           <USESPRIMARYFREQUENCY>1</USESPRIMARYFREQUENCY>" +
            "                       </REPEATER>" +
            "                       <DISCOVEREDTYPEID>f6676a4ae64d475f9a3153da9d847027</DISCOVEREDTYPEID>" +
            "                       <BESTGATEWAY>" +
            "                           <ID>a9997f8f65514570b7415753d4fe9f83</ID>" +
            "                           <NEURONID>0458243E0100</NEURONID>" +
            "                           <SERIALNUMBER>LW00023525</SERIALNUMBER>" +
            "                       </BESTGATEWAY>" +
            "                   </DEVICE>" +
            "       </DEVICES>" +
            "   </DATA>" +
            "   </RESULT>" +
            "   </APIPAYLOAD>" +
            "</RETURNS>" ;

        Document d = Util.toDom(xml);
        NodeIterator iter = Util.xPathNodeIterator(d, "/RETURNS/APIPAYLOAD/RESULT/DATA/DEVICES/DEVICE");
        
        Node n = null; 
        while( (n=iter.nextNode())!=null )
            System.out.println(n);
        
        JFrame f = new JFrame();
        f.getContentPane().add( new DeviceMasterDetailPanel(d) );
        f.show();
    }
}

/**
 *
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;

/**
 * @author gna
 *
 */
public class IPv4Setup extends AbstractCosemObject{

	private OctetString dl_Reference = null;
	private Unsigned32 ipAddress = null;
	private Array multicastIPAddress = null;
	private Array ipOptions = null;
	private Unsigned32 subnetMask = null;
	private Unsigned32 gatewayIPAddress = null;
	private boolean useDHCPFlag = true;				//TODO
	private Unsigned32 primaryDNSAddress = null;
	private Unsigned32 secondaryDNSAddress = null;


	static final byte[] LN=new byte[]{0,0,25,1,0,(byte)255};

	public IPv4Setup(ProtocolLink protocolLink) {
        super(protocolLink,new ObjectReference(LN));
    }

	public IPv4Setup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	public static ObisCode getObisCode() {
		return ObisCode.fromByteArray(LN) ;
	}

	protected int getClassId() {
		return DLMSClassId.IPV4_SETUP.getClassId();
	}

	public OctetString readDLReference() throws IOException {
		if(this.dl_Reference == null){
			this.dl_Reference = new OctetString(getLNResponseData(2));
		}
		return this.dl_Reference;
	}

	public void writeDLReference(OctetString dlReference) throws IOException{
		write(2, dlReference.getBEREncodedByteArray());
		this.dl_Reference = dlReference;
	}

	public Unsigned32 readIPAddress() throws IOException{
		if(this.ipAddress == null){
			this.ipAddress = new Unsigned32(getLNResponseData(3), 0);
		}
		return this.ipAddress;
	}

	public String getIPAddress() throws IOException{
	   	StringBuffer builder = new StringBuffer();
    	for(int i = 1; i < readIPAddress().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readIPAddress().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}

	public void setIPAddress(String newIp) throws IOException{
		int pointer = 0;
		byte[] ipByte = new byte[5];
		ipByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < ipByte.length; i++){
			ipByte[i] = (byte)Integer.parseInt(newIp.substring(pointer, (newIp.indexOf(".", pointer) == -1)?newIp.length():newIp.indexOf(".", pointer)));
			pointer = newIp.indexOf(".", pointer) + 1;
		}
		writeIPAddress(new Unsigned32(ipByte, 0));
	}

	public void writeIPAddress(Unsigned32 newIp) throws IOException{
		write(3, newIp.getBEREncodedByteArray());
		this.ipAddress = newIp;
	}

	public static void main(String args[]){
		try {
			String str = "10.0.0.214";
			IPv4Setup ipv4 = new IPv4Setup(null, null);
			ipv4.setIPAddress(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Array readMulticastIPAddress() throws IOException{
		if(this.multicastIPAddress == null){
			this.multicastIPAddress = (Array)AXDRDecoder.decode(getLNResponseData(4));

		}
		return this.multicastIPAddress;
	}

	public void writeMulticastIPAddress(Array multicastIPAddress) throws IOException{
		write(4, multicastIPAddress.getBEREncodedByteArray());
		this.multicastIPAddress = multicastIPAddress;
	}

	public Array readIPOptions() throws IOException {
		if(this.ipOptions == null){
			this.ipOptions = (Array)AXDRDecoder.decode(getLNResponseData(5));
		}
		return this.ipOptions;
	}

	public void writeIPOptions(Array ipOptions) throws IOException {
		write(5, ipOptions.getBEREncodedByteArray());
		this.ipOptions = ipOptions;
	}

	public Unsigned32 readSubnetMask() throws IOException {
		if(this.subnetMask == null){
			this.subnetMask = new Unsigned32(getLNResponseData(6), 0);
		}
		return this.subnetMask;
	}

	public String getSubnetMask() throws IOException{
	   	StringBuffer builder = new StringBuffer();
    	for(int i = 1; i < readSubnetMask().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readSubnetMask().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}

	public void writeSubnetMask(Unsigned32 subnetMask) throws IOException{
		write(6, subnetMask.getBEREncodedByteArray());
		this.subnetMask = subnetMask;
	}

	public void setSubnetMask(String subnetMask) throws IOException {
		int pointer = 0;
		byte[] subnetByte = new byte[5];
		subnetByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < subnetByte.length; i++){
			subnetByte[i] = (byte)Integer.parseInt(subnetMask.substring(pointer, (subnetMask.indexOf(".", pointer) == -1)?subnetMask.length():subnetMask.indexOf(".", pointer)));
			pointer = subnetMask.indexOf(".", pointer) + 1;
		}
		writeSubnetMask(new Unsigned32(subnetByte, 0));
	}

	public Unsigned32 readGatewayIPAddress() throws IOException {
		if(this.gatewayIPAddress == null){
			this.gatewayIPAddress = new Unsigned32(getLNResponseData(7), 0);
		}
		return this.gatewayIPAddress;
	}

	public String getGatewayIPAddress() throws IOException{
	   	StringBuffer builder = new StringBuffer();
    	for(int i = 1; i < readGatewayIPAddress().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readGatewayIPAddress().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}

	public void writeGatewayIPAddress(Unsigned32 gatewayIPAddress) throws IOException{
		write(7, gatewayIPAddress.getBEREncodedByteArray());
		this.gatewayIPAddress = gatewayIPAddress;
	}

	public void setGatewayIPAddress(String gatewayIPAddress) throws IOException {
		int pointer = 0;
		byte[] gatewayByte = new byte[5];
		gatewayByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < gatewayByte.length; i++){
			gatewayByte[i] = (byte)Integer.parseInt(gatewayIPAddress.substring(pointer, (gatewayIPAddress.indexOf(".", pointer) == -1)?gatewayIPAddress.length():gatewayIPAddress.indexOf(".", pointer)));
			pointer = gatewayIPAddress.indexOf(".", pointer) + 1;
		}
		writeGatewayIPAddress(new Unsigned32(gatewayByte, 0));
	}

	public Unsigned32 readPrimaryDNSAddress() throws IOException {
		if(this.primaryDNSAddress == null){
			this.primaryDNSAddress = new Unsigned32(getLNResponseData(9), 0);
		}
		return this.primaryDNSAddress;
	}

	public String getPrimaryDNSAddress() throws IOException{
	   	StringBuffer builder = new StringBuffer();
    	for(int i = 1; i < readPrimaryDNSAddress().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readPrimaryDNSAddress().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}

	public void writePrimaryDNSAddress(Unsigned32 primaryDNSAddress) throws IOException{
		write(9, primaryDNSAddress.getBEREncodedByteArray());
		this.primaryDNSAddress = primaryDNSAddress;
	}

	public void setPrimaryDNSAddress(String primaryDNSAddress) throws IOException {
		int pointer = 0;
		byte[] primaryDNSByte = new byte[5];
		primaryDNSByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < primaryDNSByte.length; i++){
			primaryDNSByte[i] = (byte)Integer.parseInt(primaryDNSAddress.substring(pointer, (primaryDNSAddress.indexOf(".", pointer) == -1)?primaryDNSAddress.length():primaryDNSAddress.indexOf(".", pointer)));
			pointer = primaryDNSAddress.indexOf(".", pointer) + 1;
		}
		writePrimaryDNSAddress(new Unsigned32(primaryDNSByte, 0));
	}

	public Unsigned32 readSecondaryDNSAddress() throws IOException {
		if(this.secondaryDNSAddress == null){
			this.secondaryDNSAddress = new Unsigned32(getLNResponseData(10), 0);
		}
		return this.secondaryDNSAddress;
	}

	public String getSecondaryDNSAddress() throws IOException{
	   	StringBuffer builder = new StringBuffer();
    	for(int i = 1; i < readSecondaryDNSAddress().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readSecondaryDNSAddress().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}

	public void writeSecondaryDNSAddress(Unsigned32 secondaryDNSAddress) throws IOException{
		write(10, secondaryDNSAddress.getBEREncodedByteArray());
		this.secondaryDNSAddress = secondaryDNSAddress;
	}

	public void setSecondaryDNSAddress(String secondaryDNSAddress) throws IOException {
		int pointer = 0;
		byte[] secondaryDNSByte = new byte[5];
		secondaryDNSByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < secondaryDNSByte.length; i++){
			secondaryDNSByte[i] = (byte)Integer.parseInt(secondaryDNSAddress.substring(pointer, (secondaryDNSAddress.indexOf(".", pointer) == -1)?secondaryDNSAddress.length():secondaryDNSAddress.indexOf(".", pointer)));
			pointer = secondaryDNSAddress.indexOf(".", pointer) + 1;
		}
		writePrimaryDNSAddress(new Unsigned32(secondaryDNSByte, 0));
	}

}

package com.energyict.protocolimpl.elster.opus;

public class OpusBuildPacket extends Parsers{
	/*
	 * Implementation of chapter 2) communications, 2.1 & 2.2
	 * Used only for longer data strings (more than 31 byte), a factory should be used 
	 * to call this object properly
	 */
	
	// ASCII commands used in command control and communication
	private static final char SOH =0x0001;  // start of heading
	private static final char STX =0x0002;  // start of text
	private static final char ETX =0x0003;  // end of text
	private static final char EOT =0x0004;  // end of transmission 
	private static final char ENQ =0x0005;  // enquiry
	private static final char ACK =0x0006;  // acknowledge
	private static final char CR  =0x000D;  // carriage return
	private static final char XON =0x0011;  // instruction packet control characters
	private static final char XOFF=0x0013;  // instruction packet control characters
	private static final char NAK =0x0021;  // negative acknowledge

	// packet segments (can all be set final after removal of standard constructor)
	private String command;
	private char soh;
	private int OSnumber;
	private int callNumber;
	private int packetNumber;
	private int checkSum=0;
	private char packetTerminator;
	private String dataTerminator="##";
	private String[] data;
	
	// operators	
	OpusBuildPacket(){} // mainly for testing purposes
	OpusBuildPacket(byte[] b){ // for incoming data
		process(parseBArraytoCArray(b));
	}
	OpusBuildPacket(char[] c){ // incoming data
		process(c);
	}
	OpusBuildPacket(int OS,int NNN,int PPP,String[] data, boolean CDC){ // outgoing data
		soh=SOH;
		this.OSnumber=OS;
		this.callNumber=NNN;
		this.packetNumber=PPP;
		this.data=data;
		packetTerminator=ETX;
		if(CDC){
			packetTerminator=CR;
		}
		// packet is built, but not yet serialized
		generateCheckSum();
		commandBuilder();
	}
	
	// char processing
	private void process(char[] c) {
		String s= new String(c);
		this.data=new String[8];
		int tel=-1, counter=10,packTel=0; // data sequence starts at byte 11
		if(c[packTel]!= SOH){
			packTel++;
			counter++;
		}
		soh=c[packTel]; // 0 is SOH
		OSnumber=Integer.valueOf(s.substring(1+packTel, 4+packTel));
		callNumber=Integer.valueOf(s.substring(4+packTel, 7+packTel));
		packetNumber=Integer.valueOf(s.substring(7+packTel, 10+packTel));
		while(!(c[counter]=='#' && c[counter+1]=='#')){
			if(c[counter]=='#'){
				tel++; // next data sequence (starts at index 0)
				this.data[tel]="";
			}else{
				this.data[tel]+=c[counter];
			}			
			counter++;
		}
		checkSum=Integer.valueOf(s.substring(s.length()-4, s.length()-1));
		packetTerminator=c[c.length-1];
		commandBuilder();
	}	
	
	// command structure builder
	// protected for testing purposes alone, in clean up change to private
	private void commandBuilder() {
		command=""+SOH+""+stringBuilder(OSnumber)+""+stringBuilder(callNumber)+""+stringBuilder(packetNumber)+""+processData()+""+dataTerminator+""+stringBuilder(checkSum)+""+packetTerminator;
	}
	protected String stringBuilder(int number) {
		String s=""+number;
		while(s.length()<3){
			s="0"+s;
		}
		s=s.substring(s.length()-3); // to be sure that only 3 bytes are sent back
		return s;
	}
	protected String processData() {
		String serData="";
		for(int i=0;i<8;i++){
			if((this.data.length)>0){
				serData+="#"+this.data[i];
			}else{
				serData+="#0";
			}
		}
		return serData;
	}
	
	// checksum calculations
	protected void generateCheckSum(){
		String commandTemp=""+SOH+""+stringBuilder(OSnumber)+""+stringBuilder(callNumber)+""+stringBuilder(packetNumber)+""+processData()+""+dataTerminator;
		this.checkSum=0;		
		for(int i=0; i<commandTemp.length(); i++){
			this.checkSum+=commandTemp.charAt(i);
		}
		checkSum%=256;
	}
	public boolean verifyCheckSum(){
		// check checksum of incoming data
		boolean check=false;
		int checkSum=0;
		for(int i=0; i<command.length()-4; i++){
			checkSum+=command.charAt(i);
		}
		checkSum%=256;
		if(checkSum==this.checkSum){check=true;}
		return check;
	}
	
	// verify correctness of SSS,NNN,PPP,CCC
	public boolean checkNumericalInput(){
		boolean check=true;
		if(SOH!=soh){
			check=false;
		}
		if(OSnumber<0 && OSnumber>999){
			check=false;
		}
		if(callNumber<0 && callNumber>999){
			check=false;
		}
		if(packetNumber<0 && packetNumber>999){
			check=false;
		}
		if(checkSum<0 &&  checkSum>255){
			check=false;
		}
		return check;
	}
	
	// check packet integrity, input whether command comes from CDC (true) or OS (false)
	public boolean checkPacket(boolean CDC){
		// verify checksum
		boolean check=verifyCheckSum();
		// last check is data terminator
		if(CDC){
			if (packetTerminator!=CR){
				check=false;
			}
		}else{
			if (packetTerminator!=ETX){
				check=false;
			}
		}
		return check;
	}
	
	// chapter 2.2 of datasheet, has to be used for send packages
	public void setInstructionPacket(){
		command=XON+command;
	}
	public void resetInstructionPacket(){
		if(command.charAt(0)==0x11){
			command=command.substring(1);
		}
	}
	
	
	// getters of arrays
	public char[] getCharArray(){
		return command.toCharArray();
	}
	public byte[] getByteArray(){	
		return parseCArraytoBArray(command.toCharArray());
	}
	public String[] getData(){
		return data;
	}
	
	
	/**
	 * @return the sOH
	 */
	public static char getSOH() {
		return SOH;
	}
	/**
	 * @return the sTX
	 */
	public static char getSTX() {
		return STX;
	}
	/**
	 * @return the eTX
	 */
	public static char getETX() {
		return ETX;
	}
	/**
	 * @return the eOT
	 */
	public static char getEOT() {
		return EOT;
	}
	/**
	 * @return the eNQ
	 */
	public static char getENQ() {
		return ENQ;
	}
	/**
	 * @return the aCK
	 */
	public static char getACK() {
		return ACK;
	}
	/**
	 * @return the cR
	 */
	public static char getCR() {
		return CR;
	}
	/**
	 * @return the xON
	 */
	public static char getXON() {
		return XON;
	}
	/**
	 * @return the xOFF
	 */
	public static char getXOFF() {
		return XOFF;
	}
	/**
	 * @return the nAK
	 */
	public static char getNAK() {
		return NAK;
	}
	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	/**
	 * @return the soh
	 */
	public char getSoh() {
		return soh;
	}
	/**
	 * @return the oSnumber
	 */
	public int getOSnumber() {
		return OSnumber;
	}
	/**
	 * @return the callNumber
	 */
	public int getCallNumber() {
		return callNumber;
	}
	/**
	 * @return the packetNumber
	 */
	public int getPacketNumber() {
		return packetNumber;
	}
	/**
	 * @return the checkSum
	 */
	public int getCheckSum() {
		return checkSum;
	}
	/**
	 * @return the packetTerminator
	 */
	public char getPacketTerminator() {
		return packetTerminator;
	}
	/**
	 * @return the dataTerminator
	 */
	public String getDataTerminator() {
		return dataTerminator;
	}
	
}

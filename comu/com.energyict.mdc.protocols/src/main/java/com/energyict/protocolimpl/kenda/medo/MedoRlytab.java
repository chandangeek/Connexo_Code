package com.energyict.protocolimpl.kenda.medo;


public class MedoRlytab extends Parsers{
	private char mode=0, ctb=0, imp=0;
	private short width=0;
	
	MedoRlytab(){}
	MedoRlytab(char[] c){
		mode= c[0];
		ctb= c[1];
		imp= c[2];
		char[] cs= {c[3], c[4]};
		width=parseCharToShort(cs);
	}
	MedoRlytab(char mode, char ctb, char imp, short width){
		this.mode=mode;
		this.ctb=ctb;
		this.imp=imp;
		this.width=width;
	}	
	public byte[] getbyteArray(){
		byte[] b= new byte[5];
		char[] c= getcharArray();
		b=parseCharToByte(c);
		return b;
	}
	private byte[] parseCharToByte(char[] c) {
		byte[] b= new byte[c.length];
		for (int i=0; i<c.length; i++){
			b[i]=(byte) c[i];
		}
		return b;
	}
	public char[] getcharArray(){
		char[] c= new char[5];
		c[0]=mode;
		c[1]=ctb;
		c[2]=imp;
		char[] cs=parseShortToChar(width);
		c[3]=cs[0]; // MSB
		c[4]=cs[1]; // LSB
		return c;
	}
	public String toString(){
		return "mode: "+NumberToString(mode)+
		" ctb: "+NumberToString(ctb)+
		" imp: "+NumberToString(imp)+
		" width: "+NumberToString(width)+ " ";
	}
	public char getMode(){
		return mode;
	}
	public void setMode(char mode){
		this.mode=mode;
	}
	public char getCtb(){
		return ctb;
	}
	public void setCtb(char ctb){
		this.ctb=ctb;
	}
	public char getImp(){
		return imp;
	}
	public void setImp(char imp){
		this.imp=imp;
	}
	public short getWidth(){
		return width;
	}
	public void setWidth(char[] w){
		short temp, fin;
		temp=(short) (((short) w[0])<<8);;
		fin=(short) w[1];
		width=(short) (fin+temp);
	}
	public void setWidth(byte[] w){
		short temp, fin;
		temp=(short) (((short) w[0])<<8);
		fin=(short) w[1];
		width=(short) (fin+temp);
	}
	public void setWidth(short w){
		width=w;
	}
	byte[] parseToByteArray() {
		// TODO Auto-generated method stub
		return null;
	}
}

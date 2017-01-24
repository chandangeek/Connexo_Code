package com.energyict.protocolimpl.kenda.meteor;
public class MeteorDialSettings extends Parsers implements MeteorCommandAbstract {
	private int[] cnt;
	
	MeteorDialSettings(){
		cnt=new int[0];
	}
	MeteorDialSettings(char[] c){
		process(c);
	}
	MeteorDialSettings(byte[] b){
		process(parseBArraytoCArray(b));
	}
	private void process(char[] c) {
		char[] temp=new char[4];
        int	tel=0;
		
		cnt=new int[c.length/4];
		for(int i=0; i<c.length; i+=4){
			temp[0]=c[i];
			temp[1]=c[i+1];
			temp[2]=c[i+2];
			temp[3]=c[i+3];
			cnt[tel++]=parseCharToInt(temp);
		}
	}	
	public byte[] parseToByteArray() {
		char[] c=new char[cnt.length*4];
		char[] temp=new char[4];
		int tel=0;
		for(int ii=0;ii<cnt.length; ii++){
			int i=cnt[ii];
			temp=parseIntToChar(i);
			c[tel]=temp[0];
			c[tel+1]=temp[1];
			c[tel+2]=temp[2];
			c[tel+3]=temp[3];
			tel++;
		}
		return parseCArraytoBArray(c);
	}

	public void printData() {
		int tel=0;
		for(int ii=0; ii<cnt.length; ii++){
			int i=cnt[ii];
			System.out.println("cnt"+(tel++)+":          "+i);
		}

	}

}

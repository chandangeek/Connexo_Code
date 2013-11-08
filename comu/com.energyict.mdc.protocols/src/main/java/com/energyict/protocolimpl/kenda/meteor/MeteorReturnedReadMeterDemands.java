package com.energyict.protocolimpl.kenda.meteor;
public class MeteorReturnedReadMeterDemands extends Parsers{
		private short[] meter;
		MeteorReturnedReadMeterDemands(){}
		MeteorReturnedReadMeterDemands(byte[] b){
			processReturn(parseBArraytoCArray(b));			
		}
		
		MeteorReturnedReadMeterDemands(char[] c){
			processReturn(c);
		}
		private void processReturn(char[] c){
			meter=new short[c.length/2];
			char[] c2=new char[2];
			int tel=0;
			for (int i=0; i<c.length; i+=2){
				c2[0]=c[i];
				c2[1]=c[i+1];
				meter[tel++]=parseCharToShort(c2);
			}
		}
		
		public void printData(){
			int tel=0;
			String s="";
			for(int i=0; i<meter.length; i++){
				if (tel<10){
					tel++;
					s+=NumberToString(meter[i])+" ";
				}
				if(i==meter.length-1){
					System.out.println(s);
				}else if (tel==9){
					System.out.println(s);
					s="";
					tel=0;
				}					
			}
			
		}
		/**
		 * @return the meter
		 */
		public short[] getMeter() {
			return meter;
		}
		/**
		 * @param meter the meter to set
		 */
		public void setMeter(short[] meter) {
			this.meter = meter;
		}
		byte[] parseToByteArray() {
			// TODO Auto-generated method stub
			return null;
		}
}

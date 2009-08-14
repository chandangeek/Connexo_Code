package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

public class S4sProfilePointer {

	private byte[] rawBytes;
	
	public S4sProfilePointer(byte[] profilePointer) {
		this.rawBytes = revertByteArray(profilePointer);
	}

	public int getCurrentPointer() {
		String str = new String(rawBytes)+"0";
		return Integer.valueOf(str, 16)/4;
	}
	
	private byte[] revertByteArray(byte[] array){
		byte[] reverse = new byte[array.length];
		int offset = array.length-1;
		for(int i = 0; i < array.length; i++,offset--){
			reverse[i] = array[offset];
		}
		return reverse;
	}

}

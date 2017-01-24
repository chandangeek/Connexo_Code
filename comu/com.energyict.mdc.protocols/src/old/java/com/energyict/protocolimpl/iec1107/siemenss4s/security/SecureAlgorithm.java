package com.energyict.protocolimpl.iec1107.siemenss4s.security;

public class SecureAlgorithm {

	private static byte g = Byte.parseByte("18"); //((byte) 0x12)
	
	public static int calculateAntiSeed(int seed){
		
		int crc = ((seed&0x3)<<2) | (seed>>14);	// get CRC bits
		seed = (seed>>2)&0xFFF;						// put seed right
		
		// now calculate the a11 to a8
		int a = (crc^((seed>>8)&0x0F))<<8;
		a |= ((crc^((seed>>4)&0x0F))<<4);
		a |= ((crc^(seed&0x0F)));
		
		// new get A to 12 bits by incrementing but miss out 0
		int A = (a+1)&0xFFF;
		if(A==0){
			A = 1;
		}
		
		// now do C which is remainder after devision
		int r = A;
		r = r<<5;
		for(int i = 0; i < 17; i++){
			if((r&1) == 1){
				r = r^g;
			}
			r = r >> 1;
		}
		
		int C = r&0x0F;
		
		// now do P
		int P = (C^((A>>8)&0x0F))<<8;
		P |= ((C^((A>>4)&0x0F))<<4);
		P |= (C^(A&0x0F));
		int antiSeed = ((P<<2) | ((C>>2)&0x03)) | ((C&0x03)<<14);
//		System.out.println(antiSeed);
		return antiSeed;
	}
}

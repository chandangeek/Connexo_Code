/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000;


/**
 * @author gna
 *
 */
public class PacketBuffer {
	
	private Object buffer[];
	private int pointer;
	
	private static int maxValues = 40;

	/**
	 * 
	 */
	public PacketBuffer() {
		buffer = new Object[maxValues];
		pointer = 0;
		
	}
	
	public void addMessage(String s){
		buffer[pointer++] = s;
		if(pointer == maxValues)
			pointer = 0;
		System.out.println(buffer);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

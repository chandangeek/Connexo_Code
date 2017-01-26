/**
 * 
 */
package com.energyict.protocolimpl.edf.trimarancje.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author gna
 *
 */
public class ContractsTable extends AbstractTable{

	public ContractsTable(DataFactory dataFactory) {
		super(dataFactory);
	}

	protected int getCode() {
		return 12;
	}

	protected void parse(byte[] data) throws IOException {
		System.out.println(data);
//      System.out.println("KV_DEBUG> write to file");
//      File file = new File("c://TEST_FILES/ContractsTable.bin");
//      FileOutputStream fos = new FileOutputStream(file);
//      fos.write(data);
//      fos.close();
		
		
		int offset = 0;
		
		while(true){
			if (offset == data.length){
				break;
			}
			
			int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
			System.out.println(temp);
		}
		
	}
	
	static public void main(String[] args){
		try{
			ContractsTable ct = new ContractsTable(null);
			
	        File file = new File("c://TEST_FILES/Trimaran2.bin");
	        FileInputStream fis = new FileInputStream(file);
	        byte[] data=new byte[(int)file.length()];
	        fis.read(data);
	        fis.close(); 
	        
	        ct.parse(data);
	        
		} 
		catch(IOException e){
			e.printStackTrace();
		}
	}

}

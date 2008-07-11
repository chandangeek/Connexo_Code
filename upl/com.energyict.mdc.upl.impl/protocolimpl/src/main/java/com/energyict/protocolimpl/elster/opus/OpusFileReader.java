package com.energyict.protocolimpl.elster.opus;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.energyict.protocolimpl.meteor.ComStruc;

public class OpusFileReader {
	/*
	 * File reader, aim is to test the objects properly and start to
	 * build the command structure in the Meteor class
	 */
	private ArrayList<ComStruc> commandReply= new ArrayList<ComStruc>();
	
    OpusFileReader( String directory,String filename) throws IOException {
        FileReader inputStream = null;
        try {
        	
            inputStream = new FileReader(directory+filename);
            
            int c,tel=0;
            char kar='0';
        	boolean type;
        	ComStruc comStruc;
            while ((c = inputStream.read()) != -1) {
            	if(c==(int) 'S'){
            		// start send sequence
            		type=true;
            		comStruc = new ComStruc();
            		comStruc.setType(type);
            		commandReply.add(comStruc);            		
            	}else if(c==(int) 'R'){
            		// start read sequence
            		type=false;
            		comStruc = new ComStruc();
            		comStruc.setType(type);
            		commandReply.add(comStruc);
            	}else if((c>='0' && c<='9') || (c>='A' && c<='F')){
            		tel++;
            		if (tel==1){
            			kar=0;
            			if(c>='0' && c<='9'){
            				kar=(char) (((c-'0')<<4) & 0x00F0);
            			}
            			else if (c>='A' && c<='F'){
            				kar=(char) (((c-'A'+10)<<4) & 0x00F0);
            			}
            		}
            		if(tel==2){
            			if(c>='0' && c<='9'){
            				kar=(char) (kar | ((c-'0') & 0x000F));
            			}
            			else if (c>='A' && c<='F'){
            				kar=(char) (kar | ((c-'A'+10) & 0x000F));
            			}
            			commandReply.get(commandReply.size()-1).addByte(kar);
            			tel=0; // next byte
            		}
            	}
            }            
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

	/**
	 * @return the commandReply
	 */
	public ComStruc[] getCommandReply() {
		ComStruc[] c=new ComStruc[commandReply.size()];
		int i=0;
		for (ComStruc cs:commandReply){
			c[i++]=cs;
			// prints the hex file content (hex has been read sent to a char value and then back to hex
			// System.out.println(c[i-1].getHexVals());
		}
		return c;
	}
}

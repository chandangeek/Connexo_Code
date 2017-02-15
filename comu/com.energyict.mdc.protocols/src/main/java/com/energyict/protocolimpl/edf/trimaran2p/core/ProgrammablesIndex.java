/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author gna
 *
 */
public class ProgrammablesIndex{

	List programmables;

	/**
	 *
	 */
	public ProgrammablesIndex() {
		setProgrammables(new ArrayList());
	}

	private void setProgrammables(ArrayList programmables) {
		this.programmables = programmables;
	}

	public String toString(){
        StringBuffer strBuff = new StringBuffer();
        for(int i = 0; i < getProgrammables().size(); i++) {
            Programmables obj = (Programmables)getProgrammables().get(i);
            strBuff.append(obj+"\n");
        }
        return strBuff.toString();
	}

	private List getProgrammables() {
		return this.programmables;
	}

	public Programmables getProgrammalbes(int variableName) throws IOException{
        Iterator it = getProgrammables().iterator();
        while(it.hasNext()) {
            Programmables obj = (Programmables)it.next();
            if (obj.getVariableName() == variableName){
                return obj;
            }
        }
        throw new IOException("ProgrammablesIndex, invalid variableName "+variableName);
	}

	public void addProgrammables(Programmables programmables){
		getProgrammables().add(programmables);
	}

}

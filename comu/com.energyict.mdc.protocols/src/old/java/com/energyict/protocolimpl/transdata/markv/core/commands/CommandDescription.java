/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandDescription.java
 *
 * Created on 13 oktober 2005, 11:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class CommandDescription {


    static List commandDescriptions = new ArrayList();
    static {
        commandDescriptions.add(new String[]{"RV","RV, event logbook"});
        commandDescriptions.add(new String[]{"RC","RC, load profile"});
        commandDescriptions.add(new String[]{"DI","DI, diagnostics"});
        commandDescriptions.add(new String[]{"TI","TI, set time date"});
        commandDescriptions.add(new String[]{"TC","TC, set schedule dialin time date"});
    }

    /** Creates a new instance of CommandDescription */
    public CommandDescription() {
    }

    static public String getDescriptionFor(String command) {
        for (int i=0;i<commandDescriptions.size();i++) {
            String[] strs = (String[])commandDescriptions.get(i);
            if (strs[0].compareTo(command) == 0)
                return strs[1];

        }
        return command+" (no description found)";
    }
}

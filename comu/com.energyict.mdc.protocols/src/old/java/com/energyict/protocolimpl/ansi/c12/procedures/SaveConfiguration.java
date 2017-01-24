/*
 * SaveConfiguration.java
 *
 * Created on 19 oktober 2005, 21:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

/**
 *
 * @author Koen
 */
public class SaveConfiguration extends AbstractProcedure {

    /** Creates a new instance of SaveConfiguration */
    public SaveConfiguration(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(2));
    }

}
/*
 * Procedure.java
 *
 * Created on 19 oktober 2005, 20:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

import com.energyict.protocolimpl.ansi.c12.tables.ProcedureInitiateTable;
import com.energyict.protocolimpl.ansi.c12.tables.ProcedureResponseTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIDBBitfield;

import java.io.IOException;
/**
 *
 * @author Koen
 */
abstract public class AbstractProcedure {


    static public final int SELECTOR_POST_RESPONSE=0;
    static public final int SELECTOR_POST_RESPONSE_ON_EXCEPTION=1;
    static public final int SELECTOR_NO_POST_RESPONSE=2;
    static public final int SELECTOR_POST_RESPONSE_IMMEDIATELY=3;

    static public final int RESULT_CODE_PROCEDURE_COMPLETED=0;
    static public final int RESULT_CODE_PROCEDURE_NOT_COMPLETED=1;
    static public final int RESULT_CODE_PROCEDURE_INVALID_PARAMETER=2;
    static public final int RESULT_CODE_PROCEDURE_CONFLICTS=3;
    static public final int RESULT_CODE_PROCEDURE_TIMING_CONSTRAIN=4;
    static public final int RESULT_CODE_PROCEDURE_NO_AUTHORIZATION=5;
    static public final int RESULT_CODE_PROCEDURE_UNRECOGNIZED=6;
    static public final String[] resultCodes={"Procedure completed",
                                              "Procedure accepted but not fully completed",
                                              "Invalid parameter for known procedure, procedure was ignored.",
                                              "Procedure conflicts with current device setup, procedure was ignored.",
                                              "Timing constrain, procedure was ignored.",
                                              "No authorization for requested procedure, procedure was ignored.",
                                              "Unrecognized procedure, procedure was ignored."};




    private byte[] procedureData;

    private ProcedureFactory procedureFactory;
    private ProcedureIdentification procedureIdentification;

    private int selector;
    private int sequenceNr;


    /** Creates a new instance of Procedure */
    public AbstractProcedure(ProcedureFactory procedureFactory, ProcedureIdentification procedureIdentification) {
        this.procedureFactory=procedureFactory;
        this.procedureIdentification=procedureIdentification;
    }

    protected void prepare() throws IOException {
        // override if necessary
        setProcedureData(null);
    }

    protected void parse(byte[] data) throws IOException {
        // override if necessary
    }


    public void initiateProcedure() throws IOException {
        ProcedureInitiateTable procedureInitiateTable = new ProcedureInitiateTable(getProcedureFactory().getC12ProtocolLink().getStandardTableFactory());
        TableIDBBitfield tib = new TableIDBBitfield(getProcedureIdentification().getProcedureNr(), getProcedureIdentification().isStdVsMfgFlag(), getSelector());
        procedureInitiateTable.setTableIDBBitfield(tib);
        procedureInitiateTable.setSequenceNr(getSequenceNr());
        prepare();
        // create procedure
        procedureInitiateTable.setProcedure(this);
        procedureInitiateTable.transfer();

        // get response
        if (getSelector() != SELECTOR_NO_POST_RESPONSE) {
            ProcedureResponseTable prt = getResponse();
            if (prt.getResultCode() != RESULT_CODE_PROCEDURE_COMPLETED)
                throw new IOException("AbstractProcedure, initiateProcedure, "+resultCodes[prt.getResultCode()]);
        }

    }

    private ProcedureResponseTable getResponse() throws IOException {
        ProcedureResponseTable prt = new ProcedureResponseTable(getProcedureFactory().getC12ProtocolLink().getStandardTableFactory());
        prt.build();
        if (prt.getProcedureResponseData() != null)
            parse(prt.getProcedureResponseData());
        return (ProcedureResponseTable)prt;
    }

    public ProcedureFactory getProcedureFactory() {
        return procedureFactory;
    }

    protected ProcedureIdentification getProcedureIdentification() {
        return procedureIdentification;
    }

    public byte[] getProcedureData() {
        return procedureData;
    }

    public void setProcedureData(byte[] procedureData) {
        this.procedureData = procedureData;
    }

    public int getSelector() {
        return selector;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }

    public int getSequenceNr() {
        return sequenceNr;
    }

    public void setSequenceNr(int sequenceNr) {
        this.sequenceNr = sequenceNr;
    }


}

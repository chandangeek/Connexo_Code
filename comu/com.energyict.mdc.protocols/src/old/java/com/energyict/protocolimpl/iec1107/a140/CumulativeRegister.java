package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;

public class CumulativeRegister extends Register {

    private Quantity importRegister;
    private Quantity exportRegister;

    public CumulativeRegister(A140 a140, String id, int length, int sets, int options ) {
        super(a140, id, length, sets, options );
    }

    public Quantity getExportRegister() throws IOException {
        read();
        return exportRegister;
    }

    public Quantity getImportRegister() throws IOException {
        read();
        return importRegister;
    }

    public void parse(byte[] ba) throws IOException {
        Unit uWh = Unit.get( "mWh" );
        importRegister = a140.getDataType().bcd.toQuantity(ba, 0, 5, uWh);
        exportRegister = a140.getDataType().bcd.toQuantity(ba, 5, 5, uWh);
    }

}

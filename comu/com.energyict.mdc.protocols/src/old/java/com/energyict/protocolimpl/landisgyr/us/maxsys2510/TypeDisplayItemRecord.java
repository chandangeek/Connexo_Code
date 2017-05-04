/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

class TypeDisplayItemRecord {

    String text;
    int displacement;
    int tableNo;
    int varType;
    int varSize;
    int totalChars;
    int decimalPlaces;
    int justification;
    String unitOfMeasure;

    static TypeDisplayItemRecord parse(Assembly assembly){
        TypeDisplayItemRecord tdir = new TypeDisplayItemRecord();
        
        tdir.text = assembly.stringValue( 16 );
        tdir.displacement = assembly.wordValue( );
        tdir.tableNo = assembly.byteValue( );
        tdir.varType = assembly.byteValue( );
        tdir.varSize = assembly.byteValue( );
        tdir.totalChars = assembly.byteValue( );
        tdir.decimalPlaces = assembly.byteValue( );
        tdir.justification = assembly.byteValue( );
        tdir.unitOfMeasure = assembly.stringValue( 4 );
        
        return tdir;
    }
    
    int getDecimalPlaces() {
        return decimalPlaces;
    }

    int getDisplacement() {
        return displacement;
    }

    int getJustification() {
        return justification;
    }

    int getTableNo() {
        return tableNo;
    }

    String getText() {
        return text;
    }

    int getTotalChars() {
        return totalChars;
    }

    String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    int getVarSize() {
        return varSize;
    }

    int getVarType() {
        return varType;
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        
        rslt.append( "TypeDisplayItemRcrd [ \n" );
        rslt.append( "text " + text + "\n" );
        rslt.append( "displacement " + displacement + "\n" );
        rslt.append( "tableNo " + tableNo + "\n" );
        rslt.append( "varType " + varType + "\n" );
        rslt.append( "varSize " + varSize + "\n" );
        rslt.append( "totalChars " + totalChars + "\n" );
        rslt.append( "decimalPlaces " + decimalPlaces + "\n" );
        rslt.append( "justification " + justification + "\n" );
        rslt.append( "unitOfMeasure " + unitOfMeasure + "\n" );
        rslt.append( "]" );
        
        return rslt.toString();
    }
    
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ParserFactory.java
 *
 * Created on 3 april 2007, 10:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Koen
 */
public class ParserFactory {

    Map parsers=new HashMap();

    /** Creates a new instance of ParserFactory */
    public ParserFactory() {
    }


    public void addBigDecimalParser(Parser parser) {
        parsers.put("BigDecimal",parser);
    }
    public void addDateParser(Parser parser) {
        parsers.put("Date",parser);
    }
    public void addParser(String key,Parser parser) {
        parsers.put(key,parser);
    }

    public Parser get(String key) throws IOException {
        Parser parser = (Parser)parsers.get(key);
        if (parser != null)
            return parser;
        throw new IOException("ParserFactory, parser "+key+" does not exist!");
    }

} // public class ParserFactory

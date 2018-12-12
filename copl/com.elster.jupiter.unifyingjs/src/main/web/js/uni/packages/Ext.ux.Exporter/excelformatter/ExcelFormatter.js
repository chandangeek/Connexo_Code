/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ext.ux.exporter.excelformatter.ExcelFormatter
 * @extends Ext.ux.exporter.Formatter
 * Specialised Format class for outputting .xls files
 */
Ext.define("Ext.ux.exporter.excelformatter.ExcelFormatter", {
    extend: "Ext.ux.exporter.Formatter",
    uses: [
        "Ext.ux.exporter.excelformatter.Cell",
        "Ext.ux.exporter.excelformatter.Style",
        "Ext.ux.exporter.excelformatter.Worksheet",
        "Ext.ux.exporter.excelformatter.Workbook"
    ],
    //contentType: 'data:application/vnd.ms-excel;base64,',
    //contentType: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8",
    //mimeType: "application/vnd.ms-excel",
   	mimeType: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
   	//charset:"base64",
    charset:"UTF-8",
    extension: "xls",
	
    format: function(store, config) {
      var workbook = new Ext.ux.exporter.excelformatter.Workbook(config);
      workbook.addWorksheet(store, config || {});

      return workbook.render();
    }
});
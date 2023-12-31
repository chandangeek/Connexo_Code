/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ext.ux.exporter.csvformatter.CsvFormatter
 * @extends Ext.ux.Exporter.Formatter
 * Specialised Format class for outputting .csv files
 * modification from Yogesh to extract value if renderers returning html
 */
Ext.define("Ext.ux.exporter.csvformatter.CsvFormatter", {
    extend: "Ext.ux.exporter.Formatter",
    mimeType: 'text/csv',
    charset:'UTF-8',
    separator: ";",
    extension: "csv",
    format: function(store, config) {
        this.columns = config.columns || (store.fields ? store.fields.items : store.model.prototype.fields.items);
        this.parserDiv = document.createElement("div");
        return this.getHeaders() + "\n" + this.getRows(store);
    },
    getHeaders: function(store) {
        var columns = [],
            title;
        Ext.each(this.columns, function(col) {
            var title;
            if (col.getXType() != "rownumberer") {
                if (col.text != undefined) {
                    title = col.text;
                } else if (col.name) {
                    title = col.name.replace(/_/g, " ");
                    title = Ext.String.capitalize(title);
                }
                columns.push(title);
            }
        }, this);
        return columns.join(this.separator);
    },
    getRows: function(store) {
        var rows = [];
        store.each(function(record, index) {
            var cell = this.getCell(record, index);
            if (cell !== null) {
                rows.push(cell);
            }
        }, this);

        return rows.join("\n");
    },
    getCell: function(record, index) {
        var cells = [],
            hasInvalidCells = false;
        Ext.each(this.columns, function(col) {
            var name = col.name || col.dataIndex || col.stateId;
            if (name && col.getXType() != "rownumberer") {
                if (Ext.isFunction(col.renderer)) {
                    //Sometimes value in cell can be invalid. In this case, whole record won't be added in export file.
                    try {
                        var value = col.renderer(record.get(name), {column: col}, record, index);
                        //to handle specific case if renderer returning html(img tags inside div)
                        this.parserDiv.innerHTML = value;
                        var values = [];
                        var divEls = this.parserDiv.getElementsByTagName('div');
                        if(divEls && divEls.length > 0) {
                            Ext.each(divEls, function(divEl) {
                                var innerValues = [];
                                var imgEls = divEl.getElementsByTagName('img');
                                Ext.each(imgEls, function(imgEl) {
                                    innerValues.push(imgEl.getAttribute('title'));
                                });
                                innerValues.push(divEl.innerText || divEl.textContent);
                                values.push(innerValues.join(':'));
                            });
                        } else {
                            values.push(this.parserDiv.innerText || this.parserDiv.textContent);
                        }
                        value = values.join('\n');
                    } catch(e){
                        hasInvalidCells = true;
                        //<debug>
                          console.error(e);
                        //</debug>
                    }
                } else {
                    var value = record.get(name);
                }
                //cells.push("\""+value+"\"");
                cells.push(value !== 'undefined' ? value : '');
            }
        }, this);

        return hasInvalidCells ? null : cells.join(this.separator);
    }
});
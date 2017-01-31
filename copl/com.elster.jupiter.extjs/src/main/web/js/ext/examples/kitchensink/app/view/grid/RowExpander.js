/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('KitchenSink.view.grid.RowExpander', {
    extend: 'Ext.grid.Panel',

    xtype: 'row-expander-grid',
    store: 'Companies',

    columns: [
        {text: "Company", flex: 1, dataIndex: 'company'},
        {text: "Price", renderer: Ext.util.Format.usMoney, dataIndex: 'price'},
        {text: "Change", dataIndex: 'change'},
        {text: "% Change", dataIndex: 'pctChange'},
        {xtype: 'datecolumn', text: "Last Updated", format: 'm/d/Y', dataIndex: 'lastChange'}
    ],
    width: 600,
    height: 300,

    //<example>
    exampleDescription: [
        '<p>This is an example of using the grid with a RowExpander plugin that adds the ability to have ' + 
        'a column in a grid which enables a second row body which expands/contracts.</p>' +
        '<p>The expand/contract behavior is configurable to react on clicking of the column, double click ' +
        'of the row, and/or hitting enter while a row is selected.</p>'
    ],
    //</example>

    plugins: [{
        ptype: 'rowexpander',
        rowBodyTpl : new Ext.XTemplate(
            '<p><b>Company:</b> {company}</p>',
            '<p><b>Change:</b> {change:this.formatChange}</p><br>',
            '<p><b>Summary:</b> {desc}</p>',
        {
            formatChange: function(v){
                var color = v >= 0 ? 'green' : 'red';
                return '<span style="color: ' + color + ';">' + Ext.util.Format.usMoney(v) + '</span>';
            }
        })
    }],
    collapsible: true,
    animCollapse: false,
    title: 'Expander Rows in a Collapsible Grid',
    iconCls: 'icon-grid'
});

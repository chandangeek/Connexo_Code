/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.Fields', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'fieldsToolbar',

    defaults: {
        hideLabel: true
    },
    items: [
        { xtype: 'textField' },
        { xtype: 'comboBox' },
        { xtype: 'dateField' },
        { xtype: 'numberField' }
    ]
});
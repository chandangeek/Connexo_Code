/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.toolbar.widget.VerticalFields', {
    extend: 'Ext.toolbar.Toolbar',
    xtype: 'verticalFieldsToolbar',
    vertical: true,
    defaults: {
        hideLabel: true
    },
    items: [
        { xtype: 'textField' },
        { xtype: 'comboBox' },
        { xtype: 'dateField' },
        { xtype: 'numberField' },
        { xtype: 'checkboxes' },
        { xtype: 'radioButtons' },
        { xtype: 'searchField' }
    ]
});
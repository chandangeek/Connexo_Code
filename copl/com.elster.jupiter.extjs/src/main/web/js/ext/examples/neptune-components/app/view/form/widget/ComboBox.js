/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.form.widget.ComboBox', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'comboBox',

    displayField: 'state',
    store: Ext.create('Ext.data.ArrayStore', {
        fields: ['abbr', 'state'],
        data : Ext.example.states
    }),
    fieldLabel: 'Combo Box',
    emptyText: 'Select a State...'
});
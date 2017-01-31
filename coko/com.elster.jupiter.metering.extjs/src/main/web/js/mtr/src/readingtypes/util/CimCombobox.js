/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.util.CimCombobox', {
    extend: 'Ext.form.field.ComboBox',
    xtype: 'cimcombobox',
    labelSeparator: '#',
    showCimCodes: true,
    initComponent: function () {
        var me = this;
        me.labelSeparator = ' ' + me.labelSeparator + (me.cimIndex ? me.cimIndex : '');
        me.showCimCodes && (me.listConfig = {
            itemTpl: Ext.create('Ext.XTemplate',
                '{', me.displayField, '}',
                '<div style="float: right">',
                '{', me.cimField,'}',
                '</div>'
            )
        });
        me.callParent(arguments)
    }
});
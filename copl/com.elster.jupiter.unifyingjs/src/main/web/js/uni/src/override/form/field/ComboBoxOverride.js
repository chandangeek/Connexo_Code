/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.override.ApplicationOverride
 */
Ext.define('Uni.override.form.field.ComboBoxOverride', {
    override: 'Ext.form.field.ComboBox',

    anyMatch: true,

    listeners: {
        // force re-validate on combo change
        change: function (combo) {
            combo.validate();
        }
    },

    initComponent: function () {
        var me=this;
        me.listConfig = me.listConfig || {};
        Ext.applyIf(me.listConfig, {
            getInnerTpl: function (displayField) {
                return '{' + displayField  + ':htmlEncode}';
            }
        });
        if (me.editable && Ext.isEmpty(me.emptyText)) {
            me.emptyText = Uni.I18n.translate('general.selectValue', 'UNI', 'Select a value ...');
        }

        this.callParent(arguments);
    }
});
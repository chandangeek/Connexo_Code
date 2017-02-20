/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Uni.form.field.LogLevelDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'log-level-displayfield',
    name: 'logLevel',
    fieldLabel: Uni.I18n.translate('general.logLevel', 'UNI', 'Log level'),

    requires: [
        'Uni.util.LogLevel'
    ],

    renderer: function (value, field) {
        if (Ext.isEmpty(value)) {
            return this.emptyText;
        }
        var me = this;
        return Uni.util.LogLevel.getLogLevel(value, field);
    }
});
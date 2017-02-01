/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.PasswordDisplay
 */
Ext.define('Uni.form.field.PasswordDisplay', {
    extend: 'Ext.form.FieldContainer',
    xtype: 'password-display-field',
    //   fieldLabel: Uni.I18n.translate('form.password', 'UNI', 'Password'),
    layout: {
        type: 'hbox',
        align: 'stretch'
    },

    originalValue: null,
    onlyView: true,

    items: [
        {
            xtype: 'displayfield',
            required: true,
            allowBlank: false,
            name: this.name,
            readOnly: this.readOnly,
            flex: 2.5
        }
    ],

    initComponent: function () {
        this.items[0].name = this.name;
        this.callParent(arguments);
    }

});
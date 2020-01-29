/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userSecuritySettings.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Ext.button.Button',
    ],
    alias:  'widget.userSecuritySettings',
    itemId: 'userSecuritySettings',


    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'user-security-settings-form',
                title: Uni.I18n.translate('general.userSecuritySettings', 'USR', 'User security settings'),
                ui: 'large',

                defaults: {
                    labelWidth: 250,
                },
                items: [
                    {
                        xtype: 'checkbox',
                        itemId: 'activate-lock-account',
                        name: 'lockAccountOption',
                        fieldLabel: Uni.I18n.translate('userSecuritySettings.activateAccountLocking', 'USR', 'Activate account locking'),
                        listeners: {
                            render: this.disableFields,
                            change: this.disableFields
                        }
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'num-numberOfTries',
                        name: 'failedLoginAttempts',
                        minValue: 1,
                        maxValue: 10,
                        required : true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('userSecuritySettings.lockAccountAfter', 'USR', 'Maximum failed login attempts'),
                        listeners: {
                            blur: me.fieldValidation
                        }
                    },
                    {
                        xtype: 'numberfield',
                        itemId: 'num-lockOutMinutes',
                        name: 'lockOutMinutes',
                        minValue: 1,
                        maxValue: 480,
                        required : true,
                        allowBlank: false,
                        fieldLabel: Uni.I18n.translate('userSecuritySettings.lockOutTime', 'USR', 'Account unlock time (in minutes)'),
                        listeners: {
                            blur: me.fieldValidation
                        }
                    },
                    {
                        xtype: 'container',
                        margin: '50 0 0 265',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('userSecuritySettings.save', 'USR', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'btn-save',
                                action: 'save'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('userSecuritySettings.cancel', 'USR', 'Cancel'),
                                href: '#/administration',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]

                    },

                ]
            }
        ];

        me.callParent(arguments);
    },

    disableFields: function (field, newValue, oldValue){
        this.up('form').down('#num-numberOfTries').setDisabled(!field.value);
        this.up('form').down('#num-lockOutMinutes').setDisabled(!field.value);

        this.up('form').down('#num-numberOfTries').setReadOnly(!field.value);
        this.up('form').down('#num-lockOutMinutes').setReadOnly(!field.value);
    },
    fieldValidation: function (field) {
        var value = field.getValue();

        if (Ext.isEmpty(value) || value < field.minValue || value > field.maxValue) {
            field.setValue(field.minValue);
        }
    }
});
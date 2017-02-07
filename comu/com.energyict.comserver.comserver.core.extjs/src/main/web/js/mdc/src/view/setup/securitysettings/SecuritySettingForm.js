/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.securitysettings.SecuritySettingForm', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Ext.form.field.TextArea',
        'Ext.button.Button',
        'Uni.util.FormErrorMessage'
    ],
    alias: 'widget.securitySettingForm',
    config: {
        securityHeader: null,
        deviceTypeId: null,
        deviceConfigurationId: null,
        actionButtonName: null,
        securityAction: null
    },
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'SecuritySettingPanel',
            items: [
                {
                    xtype: 'form',
                    itemId: 'myForm',
                    width: '50%',
                    defaults: {
                        labelWidth: 250,
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            xtype: 'uni-form-error-message',
                            itemId: 'mdc-security-settings-form-errors',
                            name: 'errors',
                            margin: '0 0 10 0',
                            width: 600,
                            hidden: true
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            itemId: 'name-field',
                            required: true,
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            fieldLabel: Uni.I18n.translate('general.name','MDC','Name'),
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            itemId: 'authCombobox',
                            fieldLabel: Uni.I18n.translate('securitySetting.authenticationLevel','MDC','Authentication level'),
                            queryMode: 'remote',
                            emptyText: Uni.I18n.translate('securitySettingForm.selectAuthenticationLevel','MDC','Select authentication level'),  //'Select authentication level',
                            name: 'authenticationLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            store: 'AuthenticationLevels'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            itemId: 'encrCombobox',
                            fieldLabel: Uni.I18n.translate('securitySetting.encryptionLevel', 'MDC', 'Encryption level'),
                            queryMode: 'remote',
                            emptyText: Uni.I18n.translate('securitySettingForm.selectEncryptionLevel','MDC','Select encryption level'),
                            name: 'encryptionLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            store: 'EncryptionLevels'
                        }

                    ],
                    loadRecord: function (record) {
                        //set current xxx levels in the stores
                        this.getForm().findField('authenticationLevelId').getStore().add(record.get('authenticationLevel'));
                        this.getForm().findField('encryptionLevelId').getStore().add(record.get('encryptionLevel'));
                        this.getForm().loadRecord(record);
                    },
                    buttons: [
                        {
                            xtype: 'container',
                            itemId: 'SecurityAction'
                        },
                        {
                            xtype: 'container',
                            itemId: 'SecuritySettingCancel'
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        Ext.suspendLayouts();
        var authenticationLevelStore = this.down('#authCombobox').getStore();
        var proxy = authenticationLevelStore.getProxy();
        proxy.setExtraParam('deviceType', this.deviceTypeId);
        proxy.setExtraParam('deviceConfig', this.deviceConfigurationId);

        var encryptionLevelStore = this.down('#encrCombobox').getStore();
        proxy = encryptionLevelStore.getProxy();
        proxy.setExtraParam('deviceType', this.deviceTypeId);
        proxy.setExtraParam('deviceConfig', this.deviceConfigurationId);

        this.down('#SecuritySettingCancel').add(
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                name: 'cancel',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings',
                ui: 'link'
            }
        );
        this.down('#SecuritySettingPanel').setTitle(this.securityHeader);
        this.down('#SecurityAction').add(
            {
                xtype: 'button',
                itemId: 'device-configuration-security-setting-action-btn',
                ui: 'action',
                name: 'securityaction',
                action: this.securityAction,
                text: this.actionButtonName
            }
        );
        Ext.resumeLayouts();
    }
});


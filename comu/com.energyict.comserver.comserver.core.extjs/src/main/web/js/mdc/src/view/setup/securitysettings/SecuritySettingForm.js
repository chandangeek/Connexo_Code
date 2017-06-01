/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.securitysettings.SecuritySettingForm', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Ext.form.field.TextArea',
        'Ext.button.Button',
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.property.controller.Registry'
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
                    width: 600,
                    defaults: {
                        labelWidth: 250,
                        validateOnChange: false,
                        validateOnBlur: false,
                        width: 600

                    },
                    items: [
                        {
                            xtype: 'uni-form-error-message',
                            itemId: 'mdc-security-settings-form-errors',
                            name: 'errors',
                            margin: '0 0 10 0',
                            hidden: true
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            itemId: 'name-field',
                            required: true,
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            itemId: 'securitySuiteCombobox',
                            fieldLabel: Uni.I18n.translate('securitySetting.securitySuite', 'MDC', 'Security suite'),
                            queryMode: 'remote',
                            emptyText: Uni.I18n.translate('securitySettingForm.selectSecuritySuite', 'MDC', 'Select security suite'),
                            name: 'securitySuiteId',
                            displayField: 'name',
                            valueField: 'id',
                            store: 'SecuritySuites'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            itemId: 'authCombobox',
                            fieldLabel: Uni.I18n.translate('securitySetting.authenticationLevel', 'MDC', 'Authentication level'),
                            queryMode: 'remote',
                            emptyText: Uni.I18n.translate('securitySettingForm.selectAuthenticationLevel', 'MDC', 'Select authentication level'),  //'Select authentication level',
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
                            emptyText: Uni.I18n.translate('securitySettingForm.selectEncryptionLevel', 'MDC', 'Select encryption level'),
                            name: 'encryptionLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            store: 'EncryptionLevels'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            itemId: 'requestSecurityCombobox',
                            fieldLabel: Uni.I18n.translate('securitySetting.requestSecurityLevel', 'MDC', 'Request security level'),
                            queryMode: 'remote',
                            emptyText: Uni.I18n.translate('securitySettingForm.selectRequestSecurityLevel', 'MDC', 'Select request security level'),
                            name: 'requestSecurityLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            store: 'RequestSecurityLevels'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
                            allowBlank: false,
                            itemId: 'responseSecurityCombobox',
                            fieldLabel: Uni.I18n.translate('securitySetting.responseSecurityLevel', 'MDC', 'Response security level'),
                            queryMode: 'remote',
                            emptyText: Uni.I18n.translate('securitySettingForm.selectResponseSecurityLevel', 'MDC', 'Select response security level'),
                            name: 'responseSecurityLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            store: 'ResponseSecurityLevels'
                        }

                    ],
                    loadRecord: function (record) {
                        //set current xxx levels in the stores
                        this.getForm().findField('securitySuiteId').getStore().add(record.get('securitySuite'));
                        this.getForm().findField('authenticationLevelId').getStore().add(record.get('authenticationLevel'));
                        this.getForm().findField('encryptionLevelId').getStore().add(record.get('encryptionLevel'));
                        this.getForm().findField('requestSecurityLevelId').getStore().add(record.get('requestSecurityLevel'));
                        this.getForm().findField('responseSecurityLevelId').getStore().add(record.get('responseSecurityLevel'));

                        this.getForm().loadRecord(record);
                    }
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'mdc-security-settings-form-details-title',
                    hidden: true,
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('general.attributes', 'MDC', 'Attributes'),
                            renderer: function () {
                                return ''; // No dash!
                            }
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    width: '100%'
                },

                {
                    xtype: 'form',
                    border: false,
                    itemId: 'SecuritySettingsAddEditButtonForm',
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    width: '100%',
                    defaults: {
                        labelWidth: 250
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
        var securitySuiteStore = this.down('#securitySuiteCombobox').getStore();
        var proxy = securitySuiteStore.getProxy();
        proxy.setExtraParam('deviceType', this.deviceTypeId);
        proxy.setExtraParam('deviceConfig', this.deviceConfigurationId);

        var authenticationLevelStore = this.down('#authCombobox').getStore();
        var proxy = authenticationLevelStore.getProxy();
        proxy.setExtraParam('deviceType', this.deviceTypeId);
        proxy.setExtraParam('deviceConfig', this.deviceConfigurationId);

        var encryptionLevelStore = this.down('#encrCombobox').getStore();
        proxy = encryptionLevelStore.getProxy();
        proxy.setExtraParam('deviceType', this.deviceTypeId);
        proxy.setExtraParam('deviceConfig', this.deviceConfigurationId);

        var requestSecurityLevelStore = this.down('#requestSecurityCombobox').getStore();
        proxy = requestSecurityLevelStore.getProxy();
        proxy.setExtraParam('deviceType', this.deviceTypeId);
        proxy.setExtraParam('deviceConfig', this.deviceConfigurationId);

        var responseSecurityLevelStore = this.down('#responseSecurityCombobox').getStore();
        proxy = responseSecurityLevelStore.getProxy();
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
    },

    createClientField: function(propertyInfo) {
        var me = this,
            fieldType,
            registry = Uni.property.controller.Registry,
            comp;

        Ext.suspendLayouts();
        propertyInfo.set('name', Uni.I18n.translate('securitySetting.client', 'MDC', 'Client'));
        fieldType = registry.getProperty(propertyInfo.getType());
        comp = Ext.create(fieldType, {
            property: propertyInfo,
            itemId: 'clientValue',
            labelWidth: 250,
            validateOnChange: false,
            validateOnBlur: false,
            width: '100%',
            resetButtonHidden: true,
            blankText: Uni.I18n.translate('general.required.field', 'MDC', 'This field is required')
        });
        comp.on('afterrender', function () {
            comp.clearInvalid();
        });
        me.down('form').insert(2, comp);
        if(!Ext.isEmpty(propertyInfo.getPropertyValue().get('value'))) {
            propertyInfo.set('value', propertyInfo.getPropertyValue().get('value'));
        }
        me.down('form').clientKey = propertyInfo.get('key');
        Ext.resumeLayouts(true);
    }
});


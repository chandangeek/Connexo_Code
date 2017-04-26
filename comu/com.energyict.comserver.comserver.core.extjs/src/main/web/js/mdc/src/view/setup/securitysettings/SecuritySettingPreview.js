/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.securitysettings.SecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.securitySettingPreview',
    frame: true,
    requires: [
        'Mdc.model.SecuritySetting',
        'Uni.property.form.Property',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.securitysettings.SecuritySettingsActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'security-settings-action-menu'
            }
        }
    ],

    deviceProtocolSupportSecuritySuites: undefined,
    deviceProtocolSupportsClient: undefined,

    initComponent: function () {
        var me = this,
            leftItems = [],
            rightItems = [];

        leftItems.push({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            name: 'name'
        });
        if (me.deviceProtocolSupportsClient) {
            leftItems.push({
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('securitySetting.client', 'MDC', 'Client'),
                labelWidth: 200,
                name: 'client'
            });
        }
        if (me.deviceProtocolSupportSecuritySuites) {
            leftItems.push({
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('securitySetting.securitySuite', 'MDC', 'Security suite'),
                labelWidth: 200,
                name: 'securitySuite',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
        }

        rightItems.push({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('securitySetting.authenticationLevel', 'MDC', 'Authentication level'),
            labelWidth: 200,
            name: 'authenticationLevel',
            renderer: function (value) {
                return Ext.String.htmlEncode(value.name);
            }
        });

        rightItems.push({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('securitySetting.encryptionLevel', 'MDC', 'Encryption level'),
            labelWidth: 200,
            name: 'encryptionLevel',
            renderer: function (value) {
                return Ext.String.htmlEncode(value.name);
            }
        });
        if (me.deviceProtocolSupportSecuritySuites) {
            rightItems.push({
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('securitySetting.requestSecurityLevel', 'MDC', 'Request security level'),
                labelWidth: 200,
                name: 'requestSecurityLevel',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
            rightItems.push({
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('securitySetting.responseSecurityLevel', 'MDC', 'Response security level'),
                labelWidth: 200,
                name: 'responseSecurityLevel',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
        }

        me.items = [
            {
                xtype: 'form',
                border: false,
                itemId: 'mdc-security-settings-preview-form',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                columnWidth: 0.49,
                                items: leftItems
                            },
                            {
                                columnWidth: 0.49,
                                items: rightItems
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'mdc-security-settings-preview-details-title',
                        hidden: true,
                        defaults: {
                            labelWidth: 250,
                            labelAlign: 'left'
                        },
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
                        isEdit: false,
                        layout: 'column',

                        defaults: {
                            xtype: 'container',
                            layout: 'form',
                            resetButtonHidden: true,
                            labelWidth: 200,
                            columnWidth: 0.49
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.securitysettings.SecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.securitySettingPreview',
    frame: true,


    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'security-settings-action-menu'
            }
        }
    ],

    hasSecuritySuites: undefined,

    initComponent: function () {
        var me = this,
            leftItems = [],
            rightItems = [];

        leftItems.push({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            name: 'name'
        });
        leftItems.push({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('securitySetting.authenticationLevel', 'MDC', 'Authentication level'),
            labelWidth: 200,
            name: 'authenticationLevel',
            renderer: function (value) {
                return Ext.String.htmlEncode(value.name);
            }
        });

        leftItems.push({
            xtype: 'displayfield',
            fieldLabel: Uni.I18n.translate('securitySetting.encryptionLevel', 'MDC', 'Encryption level'),
            labelWidth: 200,
            name: 'encryptionLevel',
            renderer: function (value) {
                return Ext.String.htmlEncode(value.name);
            }
        });
        if (me.hasSecuritySuites) {
            rightItems.push({
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('securitySetting.securitySuite', 'MDC', 'Security suite'),
                labelWidth: 200,
                name: 'securitySuite',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
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
                name: 'securitySettingDetails',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    layout: 'form'
                },
                items: [
                    {
                        columnWidth: 0.4,
                        items: leftItems
                    },
                    {
                        columnWidth: 0.6,
                        items: rightItems
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }

});



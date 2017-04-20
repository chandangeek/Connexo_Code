/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.securitysettings.SecuritySettingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.securitySettingGrid',
    itemId: 'securitySettingGrid',
    requires: [
        'Mdc.view.setup.securitysettings.SecuritySettingsActionMenu'
    ],
    deviceTypeId: null,
    deviceConfigId: null,
    store: 'Mdc.store.SecuritySettingsOfDeviceConfiguration',
    scroll: false,
    viewConfig: {
        style: {overflow: 'auto', overflowX: 'hidden'},
        enableTextSelection: true
    },

    deviceProtocolSupportSecuritySuites: undefined,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 2
            }
        ];

        if (me.deviceProtocolSupportSecuritySuites) {
            me.columns.push({
                header: Uni.I18n.translate('securitySetting.securitySuite', 'MDC', 'Security suite'),
                dataIndex: 'securitySuite',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
        }
        me.columns.push({
            header: Uni.I18n.translate('securitySetting.authenticationLevel', 'MDC', 'Authentication level'),
            dataIndex: 'authenticationLevel',
            flex: 3,
            renderer: function (value) {
                return Ext.String.htmlEncode(value.name);
            }
        });
        me.columns.push({
            header: Uni.I18n.translate('securitySetting.encryptionLevel', 'MDC', 'Encryption level'),
            dataIndex: 'encryptionLevel',
            flex: 3,
            renderer: function (value) {
                return Ext.String.htmlEncode(value.name);
            }
        });
        if (me.deviceProtocolSupportSecuritySuites) {
            me.columns.push({
                header: Uni.I18n.translate('securitySetting.requestSecurityLevel', 'MDC', 'Request security level'),
                dataIndex: 'requestSecurityLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
            me.columns.push({
                header: Uni.I18n.translate('securitySetting.responseSecurityLevel', 'MDC', 'Response security level'),
                dataIndex: 'responseSecurityLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            });
        }
        me.columns.push({
            xtype: 'uni-actioncolumn',
            width: 120,
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'security-settings-action-menu'
            }
        });

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                usesExactCount: true,
                displayMsg: Uni.I18n.translate('securitySettings.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} security settings'),
                displayMoreMsg: Uni.I18n.translate('securitySetting.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} security settings'),
                emptyMsg: Uni.I18n.translate('securitySettings.pagingtoolbartop.emptyMsg', 'MDC', 'There are no security settings to display'),
                dock: 'top',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-security-setting-to-device-configuration-btn',
                        text: Uni.I18n.translate('securitySetting.addSecuritySetting', 'MDC', 'Add security setting'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        action: 'addsecurityaction',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/securitysettings/add'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


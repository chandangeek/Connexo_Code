/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceSecuritySettingGrid',
    overflowY: 'auto',
    itemId: 'devicesecuritysettinggrid',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.SecuritySettingsOfDevice',
        'Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingActionMenu',
        'Uni.grid.column.Default'
    ],

    store: 'Mdc.store.SecuritySettingsOfDevice',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceSecuritySetting.name', 'MDC', 'Security setting'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('securitySetting.securitySuite', 'MDC', 'Security suite'),
                itemId: 'mdc-deviceSecuritySettingGrid-securitySuite',
                dataIndex: 'securitySuite',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                header: Uni.I18n.translate('deviceSecuritySetting.authenticationLevel', 'MDC', 'Authentication level'),
                itemId: 'mdc-deviceSecuritySettingGrid-authenticationLevel',
                dataIndex: 'authenticationLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                header: Uni.I18n.translate('deviceSecuritySetting.encryptionLevel', 'MDC', 'Encryption level'),
                itemId: 'mdc-deviceSecuritySettingGrid-encryptionLevel',
                dataIndex: 'encryptionLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                header: Uni.I18n.translate('securitySetting.requestSecurityLevel', 'MDC', 'Request security level'),
                itemId: 'mdc-deviceSecuritySettingGrid-requestSecurityLevel',
                dataIndex: 'requestSecurityLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                header: Uni.I18n.translate('securitySetting.responseSecurityLevel', 'MDC', 'Response security level'),
                itemId: 'mdc-deviceSecuritySettingGrid-responseSecurityLevel',
                dataIndex: 'responseSecurityLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                },
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Mdc.privileges.DeviceSecurity.viewOrEditLevels,
                menu: {
                    xtype: 'device-security-setting-action-menu',
                    itemId: 'deviceSecurityGridMenu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceSecuritySetting.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} security settings'),
                displayMoreMsg: Uni.I18n.translate('deviceSecuritySetting.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} security settings'),
                emptyMsg: Uni.I18n.translate('deviceSecuritySetting.pagingtoolbartop.emptyMsg', 'MDC', 'There are no security settings to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('deviceSecuritySetting.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Security settings per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    },

    updateColumns: function (hasSecuritySuite, hasClient) {
        this.down('#mdc-deviceSecuritySettingGrid-securitySuite').setVisible(hasSecuritySuite);
        this.down('#mdc-deviceSecuritySettingGrid-requestSecurityLevel').setVisible(hasSecuritySuite);
        this.down('#mdc-deviceSecuritySettingGrid-responseSecurityLevel').setVisible(hasSecuritySuite);
    }
});



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
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('securitySetting.name','MDC','Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('securitySetting.authenticationLevel','MDC','Authentication level'),
                dataIndex: 'authenticationLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                header: Uni.I18n.translate('securitySetting.encryptionLevel','MDC','Encryption level'),
                dataIndex: 'encryptionLevel',
                flex: 3,
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                items: 'Mdc.view.setup.securitysettings.SecuritySettingsActionMenu'
            }
        ];

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
                        text:  Uni.I18n.translate('securitySetting.addSecuritySetting','MDC','Add security setting'),
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


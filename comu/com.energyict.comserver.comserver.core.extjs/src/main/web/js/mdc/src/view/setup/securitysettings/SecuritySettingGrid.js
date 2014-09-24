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
        style: { overflow: 'auto', overflowX: 'hidden' }
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('securitySetting.name','MDC','Name'),
                dataIndex: 'name',
                flex: 0.2
            },
            {
                header: Uni.I18n.translate('securitySetting.authenticationLevel','MDC','Authentication level'),
                dataIndex: 'authenticationLevel',
                flex: 0.3,
                renderer: function (value) {
                    return value.name;
                }
            },
            {
                header: Uni.I18n.translate('securitySetting.encryptionLevel','MDC','Encryption level'),
                dataIndex: 'encryptionLevel',
                flex: 0.3,
                renderer: function (value) {
                    return value.name;
                }
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.securitysettings.SecuritySettingsActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                usesExactCount: true,
                displayMsg: Uni.I18n.translatePlural('securitySetting.pagingtoolbartop.displayMsg',this.store.count ,'MDC', '{2} security settings'),
                emptyMsg: Uni.I18n.translate('securitySetting.pagingtoolbartop.emptyMsg', 'MDC', 'There are no security settings'),
                dock: 'top',
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    },
                    {
                        xtype: 'button',
                        text:  Uni.I18n.translate('securitySetting.addSecuritySetting','MDC','Add security setting'),
                        action: 'addsecurityaction',                       
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/securitysettings/create'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


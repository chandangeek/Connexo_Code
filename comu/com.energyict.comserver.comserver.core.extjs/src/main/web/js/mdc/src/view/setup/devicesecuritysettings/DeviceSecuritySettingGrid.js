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

    store: 'SecuritySettingsOfDevice',

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('deviceSecuritySetting.name', 'MDC', 'Security setting'),
                dataIndex: 'name',
                flex: 0.2
            },
            {
                header: Uni.I18n.translate('deviceSecuritySetting.authenticationLevel', 'MDC', 'Authentication level'),
                dataIndex: 'authenticationLevel',
                flex: 0.3,
                renderer: function (value) {
                    return value.name;
                }
            },
            {
                header: Uni.I18n.translate('deviceSecuritySetting.encryptionLevel', 'MDC', 'Encryption level'),
                dataIndex: 'encryptionLevel',
                flex: 0.3,
                renderer: function (value) {
                    return value.name;
                }
            },
            {
                header: Uni.I18n.translate('deviceSecuritySetting.status', 'MDC', 'Status'),
                dataIndex: 'status',
                renderer: function (value) {
                    return value.name;
                },
                flex: 0.3
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingActionMenu',
                isDisabled: function(view, rowIndex, colIndex, item, record) {
                    return !record.data.userHasEditPrivilege
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
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mrid: me.mrid}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceSecuritySetting.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Security settings per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});



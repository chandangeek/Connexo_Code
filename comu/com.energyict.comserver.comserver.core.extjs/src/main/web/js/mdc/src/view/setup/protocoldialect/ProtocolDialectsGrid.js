Ext.define('Mdc.view.setup.protocoldialect.ProtocolDialectsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.protocolDialectsGrid',
    overflowY: 'auto',
    itemId: 'protocoldialectsgrid',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ProtocolDialectsOfDeviceConfiguration',
        'Mdc.view.setup.protocoldialect.ProtocolDialectActionMenu'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'ProtocolDialectsOfDeviceConfiguration',

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                menu: {xtype: 'protocol-dialect-action-menu'}
            }

        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} protocol dialects'),
                displayMoreMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} protocol dialects'),
                emptyMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.emptyMsg', 'MDC', 'There are no protocol dialects to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {deviceType: me.deviceTypeId},
                    {deviceConfig: me.deviceConfigId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('protocolDialects.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Protocol dialects per page')
            }
        ];

        me.callParent();
    }
});




Ext.define('Mdc.view.setup.deviceprotocol.DeviceProtocolDialectsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceProtocolDialectsGrid',
    overflowY: 'auto',
    itemId: 'deviceprotocoldialectsgrid',
    mRID: null,
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.ProtocolDialectsOfDevice',
        'Mdc.view.setup.deviceprotocol.DeviceProtocolDialectActionMenu'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'ProtocolDialectsOfDevice',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('protocolDialect.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                items: 'Mdc.view.setup.protocoldialect.ProtocolDialectActionMenu',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.protocolDialectsActions,
            }

        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} protocol dialects'),
                displayMoreMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} protocol dialects'),
                emptyMsg: Uni.I18n.translate('protocolDialects.pagingtoolbartop.emptyMsg', 'MDC', 'There are no protocol dialects to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {mRID: this.mRID}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('protocolDialects.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Protocol dialects per page')
            }
        ];

        this.callParent();
    }
});
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
        'Mdc.store.ProtocolDialectsOfDeviceConfiguration'
    ],
    selModel: {
        mode: 'SINGLE'
    },
    store: 'ProtocolDialectsOfDeviceConfiguration',
    listeners: {
        'render': function (component) {
            // Get sure that the store is not loading and that it
            // has at least a record on it
            if (this.store.isLoading() || this.store.getCount() == 0) {
                // If it is still pending attach a listener to load
                // event for a single time to handle the selection
                // after the store has been loaded
                this.store.on('load', function () {
                    this.getView().getSelectionModel().select(0);
                    this.getView().focusRow(0);
                }, this, {
                    single: true
                });
            } else {
                this.getView().getSelectionModel().select(0);
                this.getView().focusRow(0);
            }

        }
    },
    padding: '10 10 10 10',
    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('protocolDialect.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 0.6
            },
            {
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                        action: 'editItem'
                    }
                ]
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
                items: [
                    {
                        xtype: 'component',
                        flex: 1
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                params: [
                    {deviceType: this.deviceTypeId},
                    {deviceConfig: this.deviceConfigId}
                ],
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('protocolDialects.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Protocol dialects per page')
            }
        ];

        this.callParent();
    }
});




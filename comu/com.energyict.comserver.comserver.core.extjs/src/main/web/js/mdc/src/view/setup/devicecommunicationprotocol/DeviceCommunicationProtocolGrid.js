Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationProtocolGrid',
    overflowY: 'auto',
    itemId: 'devicecommunicationprotocolgrid',

    selModel: {
        mode: 'SINGLE'
    },

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.DeviceCommunicationProtocolsPaged',
        'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu'
    ],

    store: 'Mdc.store.DeviceCommunicationProtocolsPaged',
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
                }, this, {
                    single: true
                });
            } else {
                this.getView().getSelectionModel().select(0);
            }

        }
    },

    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);
        this.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 3
            },
            {
                header: Uni.I18n.translate('deviceCommunicationProtocols.version', 'MDC', 'Version'),
                dataIndex: 'deviceProtocolVersion',
                flex: 2
            },

            {
                xtype: 'uni-actioncolumn',
                items:'Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu',
                privileges: Mdc.privileges.Communication.admin
            }

        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceCommunicationProtocols.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communication protocols'),
                displayMoreMsg: Uni.I18n.translate('deviceCommumnicationProtocols.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communication protocols'),
                emptyMsg: Uni.I18n.translate('deviceCommunicationProtocols.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communication protocols to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceCommunicationProtocols.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communication protocols per page')
            }
        ];

        this.callParent();
    }
})
;

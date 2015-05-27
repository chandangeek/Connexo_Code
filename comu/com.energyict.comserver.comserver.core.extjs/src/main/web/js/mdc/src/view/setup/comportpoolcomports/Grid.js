Ext.define('Mdc.view.setup.comportpoolcomports.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.comportpoolcomports.ActionMenu'
    ],
    alias: 'widget.comPortPoolComPortsGrid',
    itemId: 'comPortPoolsComPortsGrid',
    store: 'Mdc.store.ComServerComPorts',
    columns: {
        items: [
            {
                header: Uni.I18n.translate('comServerComPorts.communicationPort', 'MDC', 'Communication port'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('comPortPoolComPorts.communicationServer', 'MDC', 'Communication server'),
                dataIndex: 'comServerName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('comPortPoolComPorts.portStatus', 'MDC', 'Port status'),
                dataIndex: 'active',
                renderer: function (value) {
                    if (value === true) {
                        return Uni.I18n.translate('general.active', 'MDC', 'Active');
                    } else {
                        return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                    }
                },
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.Communication.admin,
                menu: {
                    xtype: 'comPortPoolComPortsActionMenu'
                }
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            displayMsg: Uni.I18n.translate('comServerComPorts.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communication ports'),
            displayMoreMsg: Uni.I18n.translate('comServerComPorts.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communication ports'),
            emptyMsg: Uni.I18n.translate('comServerComPorts.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communication ports to display'),
            items: [
                {
                    xtype: 'button',
                    action: 'addComPort',
                    itemId: 'btn-add-comport-to-pool',
                    text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port'),
                    privileges: Mdc.privileges.Communication.admin,
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('comServerComPorts.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communication ports per page')
        }
    ],

    initComponent: function () {
        var store = this.store,
            pagingToolbarTop = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbartop';
            }),
            pagingToolbarBottom = Ext.Array.findBy(this.dockedItems, function (item) {
                return item.xtype == 'pagingtoolbarbottom';
            });

        pagingToolbarTop && (pagingToolbarTop.store = store);
        pagingToolbarBottom && (pagingToolbarBottom.store = store);

        this.callParent(arguments);
    }
});
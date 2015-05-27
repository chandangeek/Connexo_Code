Ext.define('Mdc.view.setup.comservercomports.Grid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.view.setup.comservercomports.ActionMenu'
    ],
    alias: 'widget.comServerComPortsGrid',
    itemId: 'comServerComPortsGrid',
    store: 'Mdc.store.ComServerComPorts',
    columns: {
        items: [
            {
                header: Uni.I18n.translate('comServerComPorts.communicationPort', 'MDC', 'Communication port'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('comServerComPorts.direction', 'MDC', 'Direction'),
                dataIndex: 'direction',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
                dataIndex: 'comPortType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
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
                    xtype: 'comServerComPortsActionMenu'
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
                    itemId: 'mnu-add-comport',
                    action: 'addComPort',
                    text: Uni.I18n.translate('comServerComPorts.add', 'MDC', 'Add communication port'),
                    privileges: Mdc.privileges.Communication.admin,
                    menu: {
                        xtype: 'comServerComPortsAddMenu'
                    }
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
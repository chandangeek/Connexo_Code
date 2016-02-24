Ext.define('Scs.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.servicecalls-grid',
    store: 'Scs.store.ServiceCalls',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.serviceCall', 'SCS', 'Service call'),
                dataIndex: 'number',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute().buildUrl() + '/' + record.get('number');
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('servicecalls.externalReference', 'SCS', 'External reference'),
                dataIndex: 'externalReference',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                dataIndex: 'type',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                dataIndex: 'state',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date'),
                dataIndex: 'creationTimeDisplay',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                dataIndex: 'lastModificationTimeDisplay',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
               // privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'scs-action-menu',
                    itemId: 'scs-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('serviceCalls.pagingtoolbartop.displayMsg', 'SCS', '{0} - {1} of {2} service calls'),
                displayMoreMsg: Uni.I18n.translate('serviceCalls.pagingtoolbartop.displayMoreMsg', 'SCS', '{0} - {1} of more than {2} service calls'),
                emptyMsg: Uni.I18n.translate('serviceCalls.pagingtoolbartop.emptyMsg', 'SCS', 'There are no service calls to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('serviceCalls.pagingtoolbarbottom.itemsPerPage', 'SCS', 'Service calls per page'),
                dock: 'bottom'
            }
        ]

        me.callParent(arguments);
    }
});
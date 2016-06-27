Ext.define('Wss.view.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.webservices-grid',
    store: 'Wss.store.Endpoints',
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Wss.view.ActionMenu'
    ],
    router: null,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'WSS', 'Name'),
                dataIndex: 'name',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/webserviceendpoints/view').buildUrl({endpointId: record.get('id')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
            },
            {
                header: Uni.I18n.translate('general.type', 'WSS', 'Type'),
                dataIndex: 'direction',
                flex: 1,
                renderer: function(value) {
                   return value.localizedValue;
                }
            },
            {
                header: Uni.I18n.translate('general.webservice', 'WSS', 'Webservice'),
                dataIndex: 'webServiceName',
                flex: 2,
                renderer: function(value, field, record) {
                    if(record) {
                        if (value && record.get('available')) {
                            return value;
                        } else if (value && !record.get('available')) {
                            return value + ' (' + Uni.I18n.translate('general.notAvailable', 'WSS', 'Not available') + ')';
                        }
                    }
                    return '-';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'WSS', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function(value) {
                    if(value === true) {
                        return Uni.I18n.translate('general.active', 'WSS', 'Active');
                    } else {
                        return Uni.I18n.translate('general.inactive', 'WSS', 'Inactive');
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.logLevel', 'WSS', 'Log level'),
                dataIndex: 'logLevel',
                flex: 1,
                renderer: function (value) {
                    return value.localizedValue;
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Wss.privileges.Webservices.admin,
                menu: {
                    xtype: 'webservices-action-menu'
                },
                flex: 0.5
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('webservices.pagingtoolbartop.displayMsg', 'WSS', '{0} - {1} of {2} webservice endpoints'),
                displayMoreMsg: Uni.I18n.translate('webservices.pagingtoolbartop.displayMoreMsg', 'WSS', '{0} - {1} of more than {2} webservice endpoints'),
                emptyMsg: Uni.I18n.translate('webservices.pagingtoolbartop.emptyMsg', 'WSS', 'There are no webservice endpoints to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addWebserviceEndpoint', 'WSS', 'Add webservice endpoint'),
                        privileges: Wss.privileges.Webservices.admin,
                        itemId: 'add-webservice-endpoint'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('webservices.pagingtoolbarbottom.itemsPerPage', 'WSS', 'Webservice endpoints per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    },
});
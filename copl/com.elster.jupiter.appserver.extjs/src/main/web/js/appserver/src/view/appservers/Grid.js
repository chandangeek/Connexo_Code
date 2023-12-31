/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.Grid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.appservers-grid',
    store: 'Apr.store.AppServers',
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
                header: Uni.I18n.translate('general.name', 'APR', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute('administration/appservers/overview').buildUrl({appServerName: record.get('name')});
                    return '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'APR', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'appservers-action-menu',
                    itemId: 'appservers-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('appServers.pagingtoolbartop.displayMsg', 'APR', '{0} - {1} of {2} application servers'),
                displayMoreMsg: Uni.I18n.translate('appServers.pagingtoolbartop.displayMoreMsg', 'APR', '{0} - {1} of more than {2} application servers'),
                emptyMsg: Uni.I18n.translate('appServers.pagingtoolbartop.emptyMsg', 'APR', 'There are no application servers to display'),
                items: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.addApplicationServer', 'APR', 'Add application server'),
                        privileges: Apr.privileges.AppServer.admin,
                        itemId: 'add-app-server',
                        href: '#/administration/appservers/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('appServers.pagingtoolbarbottom.itemsPerPage', 'APR', 'Application servers per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});


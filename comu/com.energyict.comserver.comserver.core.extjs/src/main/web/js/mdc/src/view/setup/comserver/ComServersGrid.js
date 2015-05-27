Ext.define('Mdc.view.setup.comserver.ComServersGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comServersGrid',
    store: Ext.getStore('Mdc.store.ComServers') || Ext.create('Mdc.store.ComServers'),
    overflowY: 'auto',
    itemId: 'comservergrid',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Action',
        'Mdc.view.setup.comserver.ActionMenu'
    ],

    columns: [
        {
            header: Uni.I18n.translate('comserver.title.communicationServer', 'MDC', 'Communication server'),
            flex: 1,
            dataIndex: 'name',
            renderer: function (value, b, record) {
                return '<a href="#/administration/comservers/' + record.get('id') + '/overview">' + Ext.String.htmlEncode(value) + '</a>';
            }
        },
        {
            header: Uni.I18n.translate('general.type', 'MDC', 'Type'),
            dataIndex: 'comServerType',
            flex: 1
        },
        {
            header: Uni.I18n.translate('comserver.status', 'MDC', 'Status'),
            dataIndex: 'active',
            width: 100,
            renderer: function (value) {
                if (value === true) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            }
        },
        {
            itemId: 'actionColumn',
            xtype: 'uni-actioncolumn',
            privileges: Mdc.privileges.Communication.admin,
            menu: {
                xtype: 'comserver-actionmenu',
                itemId: 'comserverViewMenu'
            }
        }
    ],

    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            store: 'ComServers',
            dock: 'top',
            displayMsg: Uni.I18n.translate('comserver.displayMsg', 'MDC', '{0} - {1} of {2} communication servers'),
            displayMoreMsg: Uni.I18n.translate('comserver.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communication servers'),
            items: [
                {
                    xtype: 'button',
                    itemId: 'btn-add-online-communication-server',
                    text: Uni.I18n.translate('deviceType.addOnline', 'MDC', 'Add online communication server'),
                    privileges: Mdc.privileges.Communication.admin,
                    href: '#/administration/comservers/add/online'
                }
            ]
        },
        {
            xtype: 'pagingtoolbarbottom',
            itemsPerPageMsg: Uni.I18n.translate('comserver.itemsPerPageMsg', 'MDC', 'Communication servers per page'),
            store: 'ComServers',
            dock: 'bottom'
        }
    ]
});
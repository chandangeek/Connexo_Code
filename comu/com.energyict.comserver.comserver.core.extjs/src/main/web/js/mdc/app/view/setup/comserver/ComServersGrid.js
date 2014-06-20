Ext.define('Mdc.view.setup.comserver.ComServersGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comServersGrid',
    store: 'ComServers',
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
            header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            flex: 1,
            xtype: 'templatecolumn',
            tpl: '<a href="#/administration/comservers/{id}">{name}</a>'
        },
        {
            header: Uni.I18n.translate('general.comserverType', 'MDC', 'Comserver type'),
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
            menu: {
                xtype: 'comserver-actionmenu'
            }
        }
    ],

    dockedItems: [
        {
            xtype: 'pagingtoolbartop',
            store: 'ComServers',
            dock: 'top',
            displayMsg: Uni.I18n.translate('comserver.displayMsg', 'MDC', '{0} - {1} of {2} communication servers'),
            displayMoreMsg: Uni.I18n.translate('comserver.displayMoreMsg', 'MDC', '{0} - {1} of more communication servers'),
            items: [
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('deviceType.addOnline', 'MDC', 'Add online comunication server'),
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
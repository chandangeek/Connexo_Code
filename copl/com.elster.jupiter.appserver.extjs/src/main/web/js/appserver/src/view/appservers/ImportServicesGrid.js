Ext.define('Apr.view.appservers.ImportServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.apr-import-services-grid',
    requires: [
        'Apr.view.appservers.ImportServiceActionMenu'
    ],
    width: '100%',
    maxHeight: 300,
    columns: [
        {
            header: Uni.I18n.translate('general.name', 'APR', 'Name'),
            dataIndex: 'importService',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'APR', 'Status'),
            dataIndex: 'status',
            flex: 0.8
        },
        {
            xtype: 'uni-actioncolumn',
            privileges: Apr.privileges.AppServer.admin,
            menu: {
                xtype: 'apr-import-services-action-menu',
                itemId: 'apr-import-services-action-menu'
            }
        }
    ]
});

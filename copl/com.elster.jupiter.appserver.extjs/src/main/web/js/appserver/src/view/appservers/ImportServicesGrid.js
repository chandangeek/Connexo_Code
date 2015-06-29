Ext.define('Apr.view.appservers.ImportServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.apr-import-services-grid',
    requires: [
        'Apr.view.appservers.ImportServiceActionMenu'
    ],
    width: '100%',
    maxHeight: 300,
    overflowY: 'auto',
    plugins: [
        'showConditionalToolTip',
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],

    columns: [
        {
            header: Uni.I18n.translate('general.name', 'UNI', 'Name'),
            dataIndex: 'importService',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'UNI', 'Status'),
            dataIndex: 'status',
            flex: 0.8
        },
        {
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'apr-import-services-action-menu',
                itemId: 'apr-import-services-action-menu'
            }
        }
    ]
});

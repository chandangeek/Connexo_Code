Ext.define('Apr.view.appservers.MessageServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.message-services-grid',
    requires: [
        'Apr.view.appservers.MessageServicesActionMenu'
    ],
    width: '100%',
    maxHeight: 300,
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
            dataIndex: 'messageService',
            flex: 1
        },
        {
            itemId: 'threads-column',
            header: Uni.I18n.translate('general.threads', 'APR', 'Threads'),
            dataIndex: 'numberOfThreads',
            align: 'right',
            flex: 0.6,
            emptyCellText: 1,
            editor: {
                xtype: 'numberfield',
                minValue: 1
            }
        },
        {
            xtype: 'uni-actioncolumn',
            menu: {
                xtype: 'message-services-action-menu',
                itemId: 'message-services-action-menu'
            }
        }
    ]
});

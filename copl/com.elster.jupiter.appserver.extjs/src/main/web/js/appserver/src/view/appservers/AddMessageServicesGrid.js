Ext.define('Apr.view.appservers.AddMessageServicesGrid', {
    extend: 'Ext.grid.Panel',
//    extend: 'Ext.Component',
    alias: 'widget.add-message-services-grid',
    requires: [
    ],
    width: '100%',
    maxHeight: 300,
    store: 'Apr.store.AddableMessageServices',
    plugins: [
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],

    columns: [
        {
            header: Uni.I18n.translate('general.name', 'APR', 'Name'),
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
        }
    ]
});



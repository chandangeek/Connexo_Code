Ext.define('Apr.view.appservers.MessageServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.message-services-grid',
    requires: [
        'Apr.view.appservers.MessageServicesActionMenu',
        'Apr.store.ActiveService'
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
            header: Uni.I18n.translate('general.messageService', 'APR', 'Message service'),
            dataIndex: 'messageService',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.status', 'APR', 'Status'),
            dataIndex: 'active',
            flex: 0.5,
            editor: {
                xtype: 'combobox',
                allowBlank: false,
                displayField:'displayName',
                valueField:'active',
                queryMode:'local',
                store: 'Apr.store.ActiveService'
            },
            renderer: function(value){
                return value?Uni.I18n.translate('general.active', 'APR', 'Active'):Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
            }
        },
        {
            itemId: 'threads-column',
            header: Uni.I18n.translate('general.threads', 'APR', 'Threads'),
            dataIndex: 'numberOfThreads',
            align: 'right',
            flex: 0.5,
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

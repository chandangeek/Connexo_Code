Ext.define('Apr.view.messageQueues.MessageQueuesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.message-queues-grid',
    store: 'Apr.store.MessageQueues',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    plugins: [
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'APR', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.retires', 'APR', 'Retries'),
                dataIndex: 'numberOfRetries',
                flex: 1,
                editor: {
                    xtype: 'numberfield',
                    minValue: 1
                }
            },
            {
                header: Uni.I18n.translate('general.timeBeforeRetry', 'APR', 'Time before retry'),
                dataIndex: 'retryDelayInSeconds',
                flex: 1,
                editor: {
                    xtype: 'numberfield',
                    minValue: 1
                },
                renderer: function(value){
                    return Uni.I18n.translatePlural('general.second',value,'APR', '{0} seconds','{0} second','{0} seconds')
                }
            }
        ];


        me.callParent(arguments);
    }
});

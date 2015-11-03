Ext.define('Apr.view.messagequeues.MonitorSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.monitor-setup',
    router: null,
    appServerName: null,

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Apr.view.messagequeues.Menu'
    ],
    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'message-queues-menu',
                //    itemId: 'message-queues-menu',
                    router: me.router
                }
            ]
        };


        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.monitor', 'APR', 'Monitor'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'monitor-grid',
                        itemId: 'monitor-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-app-server',
                        title: Uni.I18n.translate('messageQueues.empty.title', 'APR', 'No message queues found'),
                        reasons: [
                            Uni.I18n.translate('messageQueues.empty.list.item1', 'APR', 'There are no message queues in the system')
                        ]
                    },
                    previewComponent: {
                        xtype: 'monitor-preview',
                        itemId: 'monitor-preview',
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
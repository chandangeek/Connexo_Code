Ext.define('Apr.view.messagequeues.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.message-queue-setup',
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
            title: Uni.I18n.translate('general.overview', 'APR', 'Overview'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'message-queues-grid',
                        itemId: 'message-queues-grid',
                        router: me.router,
                        dockedItems: [
                            {
                                xtype: 'pagingtoolbartop',
                                dock: 'top',
                                displayMsg: '',
                                displayMoreMsg: '',
                                emptyMsg: '',
                                exportButton: false,
                                items: [
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.saveSettings', 'APR', 'Save settings'),
                                        itemId: 'save-message-queues-button',
                                        privileges: Apr.privileges.AppServer.admin,
                                        disabled: true
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.undo', 'APR', 'Undo'),
                                        itemId: 'undo-message-queues-changes-button',
                                        privileges: Apr.privileges.AppServer.admin,
                                        disabled: true
                                    }
                                ]
                            }
                        ]
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-app-server',
                        title: Uni.I18n.translate('messageQueues.empty.title', 'APR', 'No message queues found'),
                        reasons: [
                            Uni.I18n.translate('messageQueues.empty.list.item1', 'APR', 'There are no message queues in the system')
                        ]
                    },
                    //previewComponent: {
                    //    xtype: 'monitor-preview',
                    //    itemId: 'monitor-preview',
                    //    router: me.router
                    //}
                }
            ]
        };

        me.callParent(arguments);
    }
});
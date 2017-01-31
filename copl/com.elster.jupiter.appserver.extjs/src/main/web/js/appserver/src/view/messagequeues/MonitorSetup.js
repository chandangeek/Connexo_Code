/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.MonitorSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.monitor-setup',
    router: null,
    appServerName: null,

    requires: [
        'Uni.util.FormEmptyMessage',
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
                        xtype: 'uni-form-empty-message',
                        itemId: 'ctr-no-app-server',
                        text: Uni.I18n.translate('messageQueues.empty', 'APR', 'There are no message queues in the system')
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
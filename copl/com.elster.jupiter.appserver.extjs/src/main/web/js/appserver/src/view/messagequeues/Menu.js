/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.message-queues-menu',

    router: null,

    title: Uni.I18n.translate('general.messageQueues', 'APR', 'Message queues'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'APR', 'Overview'),
                itemId: 'queue-overview-link',
                href: me.router.getRoute('administration/messagequeues').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.monitor', 'APR', 'Monitor'),
                itemId: 'queue-monitor-link',
                href: me.router.getRoute('administration/messagequeues/monitor').buildUrl()
            }
        ];

        me.callParent(arguments);
    }

});

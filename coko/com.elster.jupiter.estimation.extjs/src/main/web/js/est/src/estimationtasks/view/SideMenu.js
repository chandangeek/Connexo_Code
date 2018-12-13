/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.estimationtasks-side-menu',
    router: null,
    title: Uni.I18n.translate('general.estimationTask', 'EST', 'Estimation task'),
    objectType: Uni.I18n.translate('general.estimationTask', 'EST', 'Estimation task'),

    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'EST', 'Details'),
                itemId: 'estimationtasks-overview-link',
                href: me.router.getRoute(me.detailRoute).buildUrl({taskId: me.taskId})
            },
            {
                text: Uni.I18n.translate('estimationtasks.general.history', 'EST', 'History'),
                itemId: 'estimationtasks-history-link',
                href: me.router.getRoute(me.historyRoute).buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});

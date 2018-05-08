/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.ctk-tasks-menu',
    router: null,
    title: Uni.I18n.translate('customTask.general.task', 'APR', 'Task'),

    initComponent: function () {
        var me = this;

        me.items = [
            {
                text: Uni.I18n.translate('general.details', 'APR', 'Details'),
                itemId: 'tasks-view-link',
                href: me.router.getRoute(me.detailRoute).buildUrl({taskId: me.taskId})
            }
        ];

        if (me.router.arguments.taskId) {
            me.items.push(
                {
                    text: Uni.I18n.translate('customTask.general.history', 'APR', 'History'),
                    itemId: 'tasks-history-link',
                    href: me.router.getRoute(me.historyRoute).buildUrl(),
                    privileges: me.canHistory
                }
            );
        }

        me.callParent(arguments);
    }
});


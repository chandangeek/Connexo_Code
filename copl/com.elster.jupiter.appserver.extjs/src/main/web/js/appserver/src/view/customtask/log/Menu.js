/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.log.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.ctk-log-menu',
    router: null,
    title: Uni.I18n.translate('customTask.general.task', 'APR', 'Task'),
    initComponent: function () {
        var me = this;

        if (me.router.arguments.taskId) {
            me.items = [
                {
                    text: Uni.I18n.translate('general.details', 'APR', 'Details'),
                    itemId: 'tasks-view-link',
                    href: me.router.getRoute(me.detailRoute).buildUrl({taskId: me.taskId})
                },
                {
                    text: Uni.I18n.translate('general.log', 'APR', 'Log'),
                    itemId: 'tasks-log-link',
                    href: me.router.getRoute('administration/taskmanagement/view/history/occurrence').buildUrl()
                }
            ];
        }
        me.callParent(arguments);
    }
});



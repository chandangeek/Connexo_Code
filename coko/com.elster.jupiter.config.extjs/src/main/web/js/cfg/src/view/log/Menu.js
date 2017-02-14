/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.log.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.log-menu',
    router: null,
    title: Uni.I18n.translate('validationTasks.general.validationTask', 'CFG', 'Validation task'),

    initComponent: function () {
        var me = this;

        if (me.router.arguments.taskId) {
            me.menuItems = [
                {
                    text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                    itemId: 'tasks-view-link',
                    href: '#/administration/validationtasks/' + me.router.arguments.taskId
                }
            ];

            me.menuItems.push(
                {
                    text: Uni.I18n.translate('validationTasks.general.log', 'CFG', 'Log'),
                    itemId: 'tasks-log-link',
                    href: me.router.getRoute('administration/validationtasks/validationtask/history/occurrence').buildUrl()
                }
            );
        }
        me.callParent(arguments);
    }
});



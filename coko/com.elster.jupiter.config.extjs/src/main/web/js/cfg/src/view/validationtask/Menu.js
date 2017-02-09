/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.cfg-tasks-menu',

    router: null,
    title: Uni.I18n.translate('validationTasks.general.validationTask', 'CFG', 'Validation task'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                itemId: 'tasks-view-link',
                href: '#/administration/validationtasks/' + me.taskId
            }
        ];

		if (me.router.arguments.taskId) {
            me.menuItems.push(
                {
                    text: Uni.I18n.translate('validationTasks.general.history', 'CFG', 'History'),
                    itemId: 'tasks-history-link',
                    href: me.router.getRoute('administration/validationtasks/validationtask/history').buildUrl()
                }
            );
        }
		
        me.callParent(arguments);
    }
});


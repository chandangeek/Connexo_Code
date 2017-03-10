/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.dxp-tasks-menu',

    router: null,

    title: Uni.I18n.translate('general.exportTask', 'DES', 'Export task'),
    objectType: Uni.I18n.translate('general.exportTask', 'DES', 'Export task'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'DES', 'Details'),
                itemId: 'tasks-view-link',
                href:  '#/administration/dataexporttasks/' + this.taskId
            }
        ];

        if (me.router.arguments.taskId) {
            me.menuItems.push(
                {
                    text: Uni.I18n.translate('general.history', 'DES', 'History'),
                    itemId: 'tasks-history-link',
                    href: me.router.getRoute('administration/dataexporttasks/dataexporttask/history').buildUrl()
                },
                {
                    text: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
                    itemId: 'tasks-data-sources-link',
                    href: me.router.getRoute('administration/dataexporttasks/dataexporttask/datasources').buildUrl()
                }
            );
        }

        me.callParent(arguments);
    },

    removeDataSourcesMenuItem: function() {
        var me = this;
        if (me.router.arguments.taskId) {
            me.remove('tasks-data-sources-link');
        }
    }
});


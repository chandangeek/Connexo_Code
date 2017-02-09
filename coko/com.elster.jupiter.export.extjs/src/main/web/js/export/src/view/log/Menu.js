/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.log.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.dxp-log-menu',
    router: null,
    title: Uni.I18n.translate('general.dataExportTask', 'DES', 'Data export task'),
    initComponent: function () {
        var me = this;

        if (me.router.arguments.taskId) {
            me.menuItems = [
                {
                    text: Uni.I18n.translate('general.details', 'DES', 'Details'),
                    itemId: 'tasks-view-link',
                    href: '#/administration/dataexporttasks/' + me.router.arguments.taskId
                }
            ];

            me.menuItems.push(
                {
                    text: Uni.I18n.translate('general.log', 'DES', 'Log'),
                    itemId: 'tasks-log-link',
                    href: me.router.getRoute('administration/dataexporttasks/dataexporttask/history/occurrence').buildUrl()
                }
            );
        }
        me.callParent(arguments);
    }
});



Ext.define('Dxp.view.tasks.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.tasks-menu',

    router: null,

    title: Uni.I18n.translate('general.dataExportTasks', 'DES', 'Data export tasks'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
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
    }
});


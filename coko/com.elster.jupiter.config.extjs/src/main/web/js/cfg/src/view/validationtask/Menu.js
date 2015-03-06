Ext.define('Cfg.view.validationtask.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.tasks-menu',

    router: null,

    title: Uni.I18n.translate('dataValidationTasks.general.dataValidationTasks', 'CFG', 'Data validation tasks'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('dataValidationTasks.general.overview', 'CFG', 'Overview'),
                itemId: 'tasks-view-link',
                href:  '#/administration/datavalidationtasks/' + this.taskId
            }
        ];
/*
        if (me.router.arguments.taskId) {
            me.menuItems.push(
                {
                    text: Uni.I18n.translate('dataValidationTasks.general.history', 'CFG', 'History'),
                    itemId: 'tasks-history-link',
                    href: me.router.getRoute('administration/datavalidationtasks/datavalidationtask/history').buildUrl()
                },
                {
                    text: Uni.I18n.translate('dataValidationTasks.general.dataSources', 'CFG', 'Data sources'),
                    itemId: 'tasks-data-sources-link',
                    href: me.router.getRoute('administration/datavalidationtasks/datavalidationtask/datasources').buildUrl()
                }
            );
        }
*/
        me.callParent(arguments);
    }
});


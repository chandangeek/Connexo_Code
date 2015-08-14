Ext.define('Cfg.view.validationtask.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.cfg-tasks-menu',

    router: null,
    title: Uni.I18n.translate('validationTasks.general.validationTasks', 'CFG', 'Validation tasks'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'CFG', 'Overview'),
                itemId: 'tasks-view-link',
                href:  '#/administration/validationtasks/' + this.taskId
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


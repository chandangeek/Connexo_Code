Ext.define('Est.estimationtasks.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.estimationtasks-side-menu',

    router: null,
    title: Uni.I18n.translate('general.estimationTask', 'EST', 'Estimation task'),

    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'EST', 'Overview'),
                itemId: 'estimationtasks-overview-link',
                href: me.router.getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: me.taskId})
            },
            {
                text: Uni.I18n.translate('estimationtasks.general.history', 'EST', 'History'),
                itemId: 'estimationtasks-history-link',
                href: me.router.getRoute('administration/estimationtasks/estimationtask/history').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});

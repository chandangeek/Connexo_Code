Ext.define('Dxp.view.tasks.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.tasks-menu',
    toggle: null,
    router: null,

    taskId: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);
        Ext.suspendLayouts();
        me.add(
            {
                text: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
                itemId: 'tasks-view-link',
                href:  '#/administration/dataexporttasks/' + this.taskId,
                hrefTarget: '_self'
            }
        );

        if (me.router.arguments.taskId) {
            me.add({
                text: Uni.I18n.translate('general.history', 'DES', 'History'),
                itemId: 'tasks-history-link',
                href: me.router.getRoute('administration/dataexporttasks/dataexporttask/history').buildUrl(),
                hrefTarget: '_self'
            });
            me.add(
                {
                    text: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
                    href: me.router.getRoute('administration/dataexporttasks/dataexporttask/datasources').buildUrl(),
                    hrefTarget: '_self'
                }
            );
        }
        Ext.resumeLayouts();
        me.toggleMenuItem(me.toggle);
    }
});


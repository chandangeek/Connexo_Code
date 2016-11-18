Ext.define('Imt.usagepointlifecycle.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.usagepoint-life-cycles-side-menu',
    xtype: 'usagepoint-life-cycles-side-menu',
    router: null,
    title: Uni.I18n.translate('general.usagePointLifeCycle', 'IMT', 'Usage point life cycle'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.usagePointLifeCycle', 'IMT', 'Usage point life cycle'),
                itemId: 'usagepoint-life-cycle-link',
                href: me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.states', 'IMT', 'States'),
                itemId: 'usagepoint-life-cycles-states-link',
                href: me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/states').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.transitions', 'IMT', 'Transitions'),
                itemId: 'usagepoint-life-cycles-transitions-link',
                href: me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions').buildUrl()                
            }
        ];

        me.callParent(arguments);
    }
});


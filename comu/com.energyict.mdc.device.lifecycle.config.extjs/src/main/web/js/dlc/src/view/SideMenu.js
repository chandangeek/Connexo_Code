Ext.define('Dlc.view.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.device-life-cycles-side-menu',
    router: null,

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.states', 'DLC', 'States'),
                itemId: 'device-life-cycles-states-link',
                href: me.router.getRoute('administration/devicelifecycles/devicelifecycle/states').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.transitions', 'DLC', 'Transitions'),
                itemId: 'device-life-cycles-transitions-link',
                href: me.router.getRoute('administration/devicelifecycles/devicelifecycle/transitions').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});


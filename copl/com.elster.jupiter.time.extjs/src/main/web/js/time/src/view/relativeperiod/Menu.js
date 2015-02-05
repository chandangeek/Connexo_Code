Ext.define('Tme.view.relativeperiod.Menu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.relative-periods-menu',

    router: null,

    title: Uni.I18n.translate('general.relativePeriods', 'DES', 'Relative periods'),

    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
                itemId: 'relative-period-overview-link',
                href: me.router.getRoute('administration/relativeperiods/relativeperiod').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});



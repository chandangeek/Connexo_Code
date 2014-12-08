Ext.define('Tme.view.relativeperiod.Menu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.relative-periods-menu',
    toggle: null,
    router: null,

    initComponent: function () {
        var me = this;
        me.callParent(me);

        me.add(
            {
                text: Uni.I18n.translate('general.overview', 'DES', 'Overview'),
                href: me.router.getRoute('administration/relativeperiods/relativeperiod').buildUrl(),
                hrefTarget: '_self'
            }
        );

        me.toggleMenuItem(me.toggle);
    }
});



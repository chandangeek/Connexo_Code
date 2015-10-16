Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.metrology-configuration-side-menu',
    router: null,
    title: Uni.I18n.translate('metrologyconfiguration.metrologyConfigurationLabel', 'IMT', 'Metrology Configuration'),
    
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('metrologyconfiguration.overview', 'IMT', 'Overview'),
                itemId: 'metrology-configuration-overview-link',
                href: me.router.getRoute('metrologyconfiguration/view').buildUrl({id: me.id})
            },
        ];
        me.callParent(arguments);
    }
});

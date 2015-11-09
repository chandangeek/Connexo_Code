Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigValRulesSetSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.metrology-config-valrulesset-side-menu',
    router: null,
    title: Uni.I18n.translate('metrologyconfiguration.metrologyConfigurationLabel', 'IMT', 'Metrology Configuration'),
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('metrologyconfiguration.overview', 'IMT', 'Overview'),
                itemId: 'metrology-configuration-overview-link',
                href: me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: me.mcid})
            },           
            {
                title: 'Configurations',
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('metrologyconfiguration.validationRuleSets', 'IMT', 'Validation Rule Sets'),
                        itemId: 'metrology-configuration-validation-rulesets-link',
                        href: me.router.getRoute('administration/metrologyconfiguration/manage').buildUrl({mcid: me.mcid})
                    },
                ]
            }
        ];
        me.callParent(arguments);
    }
});

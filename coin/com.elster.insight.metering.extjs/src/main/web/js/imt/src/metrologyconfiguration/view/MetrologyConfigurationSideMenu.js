Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.metrology-configuration-side-menu',
    requires: [
               'Imt.privileges.UsagePoint'
           ],
    router: null,
    title: Uni.I18n.translate('metrologyconfiguration.label.metrologyconfiguration', 'IMT', 'Metrology Configuration'),
    initComponent: function () {
        var me = this;
        var casRoute = me.router.getRoute('administration/metrologyconfiguration/view/customAttributeSets');

        me.menuItems = [
            {
                text: Uni.I18n.translate('general.label.overview', 'IMT', 'Overview'),
                itemId: 'metrology-configuration-overview-link',
                href: me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: me.mcid})
            },
            {
                text: casRoute.getTitle(),
                itemId: 'metrology-configuration-custom-attribute-sets-link',
                href: casRoute.buildUrl()
            },
            {
                title: 'Configurations',
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('metrologyconfiguration.label.validationRuleSets', 'IMT', 'Validation Rule Sets'),
                        privileges: Imt.privileges.UsagePoint.admin,
                        itemId: 'metrology-configuration-validation-rulesets-link',
                        href: me.router.getRoute('administration/metrologyconfiguration/view/manage').buildUrl({mcid: me.mcid})
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

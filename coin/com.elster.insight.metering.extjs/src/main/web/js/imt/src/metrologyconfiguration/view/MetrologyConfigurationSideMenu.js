Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.metrology-configuration-side-menu',
    title: Uni.I18n.translate('metrologyconfiguration.label.metrologyconfiguration', 'IMT', 'Metrology Configuration'),

    router: null,
    metrologyConfig: null,

    initComponent: function () {
        var me = this;
            // out of scope CXO-633
            //purposes = [],
            //requiredIcon = '<span class="uni-form-item-label-required" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
            //    + Uni.I18n.translate('general.requiredPurpose', 'IMT', 'Required purpose')
            //    + '"></span>';

        //me.metrologyConfig.metrologyContracts().each(function (metrologyContract) {
        //    purposes.push({
        //        text: metrologyContract.get('name') + (metrologyContract.get('mandatory') ? ' ' + requiredIcon : ''),
        //        itemId: 'metrology-configuration-purpose-link-' + metrologyContract.getId(),
        //        htmlEncode: false
        //    });
        //});

        me.menuItems = [
            {
                text: me.metrologyConfig.get('name'),
                itemId: 'metrology-configuration-overview-link',
                href: me.router.getRoute('administration/metrologyconfiguration/view').buildUrl()
            },
            // out of scope CXO-633
            //{
            //    text: Uni.I18n.translate('general.inputs', 'IMT', 'Inputs'),
            //    itemId: 'metrology-configuration-inputs-link'
            //},
            //purposes.length ? {
            //    title: Uni.I18n.translate('general.purposes', 'IMT', 'Purposes'),
            //    xtype: 'menu',
            //    items: purposes
            //} : null,
            {
                title: Uni.I18n.translate('general.customAttributes', 'IMT', 'Custom attributes'),
                xtype: 'menu',
                items: [
                    {
                        text: Uni.I18n.translate('general.customAttributeSets', 'IMT', 'Custom attribute sets'),
                        itemId: 'metrology-configuration-custom-attribute-sets-link',
                        href: me.router.getRoute('administration/metrologyconfiguration/view/customAttributeSets').buildUrl()
                    }
                ]
            },
            {
                title: Uni.I18n.translate('general.readingQualities', 'IMT', 'Reading qualities'),
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

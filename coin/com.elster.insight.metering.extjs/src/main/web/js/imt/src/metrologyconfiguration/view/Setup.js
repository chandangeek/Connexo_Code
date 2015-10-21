Ext.define('Imt.metrologyconfiguration.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-setup',
    itemId: 'metrology-configuration-setup',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationSideMenu',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm'
    ],
    router: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'metrologyConfigurationSetupPanel',
            layout: {
                type: 'fit',
//                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
//        panel.title = me.router.getRoute().getTitle();
//        me.side = [
//            {
//                xtype: 'panel',
//                ui: 'medium',
//                items: [
//                    {
//                        xtype: 'metrology-configuration-side-menu',
//                        itemId: 'metrology-configuration-side-menu',
//                        router: me.router,
//  //                      id: me.id
//                    }
//                ]
//            }
//        ];
        this.callParent(arguments);

        me.down('#metrologyConfigurationSetupPanel').add(
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('metrologyConfiguration.attributes', 'IMT', 'Metrology Configuration Attributes'),
                        ui: 'tile',
                        itemId: 'metrology-configuration-attributes-panel',
                        router: me.router,
//                        id: me.id,
                    }
                ]
            }
        );
    }
});
Ext.define('Imt.purpose.view.Outputs', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.purpose-outputs',
    itemId: 'purpose-outputs',
    requires: [
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.purpose.view.OutputsList'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                itemId: 'metrologyConfigurationListSetupPanel',
                title: me.router.getRoute().getTitle(),
                layout: {
                    type: 'fit',
                    align: 'stretch'
                },

                items: [{
                    xtype: 'emptygridcontainer',
                    grid: {
                        xtype: 'outputs-list'
//                        router: me.router,
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'ctr-no-metrology-configurations',
                        title: Uni.I18n.translate('metrologyconfiguration.list.empty', 'IMT', 'No metrology configurations found'),
                        reasons: [
                            Uni.I18n.translate('metrologyconfiguration.list.undefined', 'IMT', 'No metrology configurations have been defined yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('metrologyconfiguration.general.add', 'IMT', 'Add metrology configuration'),
//                                privileges : Cfg.privileges.Validation.admin,
                                href: '#/administration/metrologyconfiguration/add'
                            }
                        ]
                    }
                }]
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                style: {
                    paddingRight: 0
                },
                items: [
                    {
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
                        usagePoint: me.usagePoint,
                        purposes: me.purposes
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
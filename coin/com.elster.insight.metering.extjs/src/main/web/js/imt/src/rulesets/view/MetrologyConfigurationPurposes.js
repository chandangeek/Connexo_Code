Ext.define('Imt.rulesets.view.MetrologyConfigurationPurposes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-purposes',
    requires: [
        'Cfg.view.validation.RuleSetSubMenu',
        'Imt.rulesets.view.MetrologyConfigurationPurposesGrid',
        'Imt.rulesets.view.MetrologyConfigurationPurposeDetails',
        'Imt.rulesets.view.MetrologyConfigurationPurposeActionMenu',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    purposesStore: null,
    router: null,
    ruleSetId: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'metrology-configuration-purposes-panel',
                title: Uni.I18n.translate('general.metrologyConfigurationPurposes', 'IMT', 'Metrology configuration purposes'),
                items: {
                    xtype: 'preview-container',
                    itemId: 'metrology-configuration-purposes-preview-container',
                    grid: {
                        xtype: 'metrology-configuration-purposes-grid',
                        itemId: 'metrology-configuration-purposes-grid',
                        store: me.purposesStore,
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-purposes-found-panel',
                        title: Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item', 'IMT', 'No metrology configuration purposes found'),
                        reasons: [
                            Uni.I18n.translate('ruleSet.metrologyConfigurationPurposes.empty.list.item1', 'IMT', 'No metrology configurations have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addMetrologyConfigurationPurposes', 'IMT', 'Add metrology configuration purposes'),
                                action: 'addMetrologyConfigurationPurposes',
                                href: me.router
                                    .getRoute('administration/rulesets/overview/metrologyconfigurationpurposes/add')
                                    .buildUrl(),
                                itemId: 'empty-msg-add-metrology-configuration-purposes-button',
                                privileges: Imt.privileges.MetrologyConfig.adminValidation
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'metrology-configuration-purpose-details',
                        itemId: 'metrology-configuration-purpose-preview',
                        frame: true,
                        tools: [
                            {
                                xtype: 'uni-button-action',
                                privileges: Imt.privileges.MetrologyConfig.adminValidation,
                                menu: {
                                    xtype: 'metrology-configuration-purpose-action-menu',
                                    itemId: 'metrology-configuration-purpose-action-menu'
                                }
                            }
                        ]
                    }
                }
            }
        ];

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'ruleSetSubMenu',
                        itemId: 'stepsMenu',
                        ruleSetId: me.ruleSetId
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
Ext.define('Imt.rulemetrologyconfiguration.view.MetrologyConfigurationPurposes', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.metrology-configuration-purposes',
    requires: [
        'Cfg.view.validation.RuleSetSubMenu',
        'Imt.rulemetrologyconfiguration.view.MetrologyConfigurationPurposesGrid',
        'Imt.rulemetrologyconfiguration.view.MetrologyConfigurationPurposeDetails',
        'Imt.rulemetrologyconfiguration.view.MetrologyConfigurationPurposeActionMenu',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
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
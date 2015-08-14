Ext.define('Mdc.deviceconfigurationestimationrules.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-configuration-estimation-rule-sets-setup',
    itemId: 'device-configuration-estimation-rule-sets-setup',

    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Mdc.deviceconfigurationestimationrules.view.RuleSetsGrid'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        itemId: 'stepsMenu',
                        deviceTypeId: me.router.arguments.deviceTypeId,
                        deviceConfigurationId: me.router.arguments.deviceConfigurationId
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.estimationRuleSets', 'MDC', 'Estimation rule sets'),
                items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-configuration-estimation-rule-sets-grid',
                        router: me.router,
                        itemId: 'estimationRuleSetsGrid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.noItems', 'MDC', 'No estimation rule sets found'),
                        reasons: [
                            Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.empty.list.item1', 'MDC', 'No estimation rule sets have been added yet.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('estimationRuleSet.add', 'MDC', 'Add estimation rule sets'),
                                action: 'addEstimationRuleSet',
                                privileges : Mdc.privileges.DeviceConfigurationEstimations.administrate
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'container',
                        itemId: 'rulesPlaceholder'
                    }
                }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

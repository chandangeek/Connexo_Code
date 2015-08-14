Ext.define('Mdc.deviceconfigurationestimationrules.view.AddRuleSetsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-configuration-estimation-add-rule-sets-setup',
    itemId: 'device-configuration-estimation-add-rule-sets-setup',

    requires: [
        'Mdc.deviceconfigurationestimationrules.view.RuleSetsBulkSelectionGrid',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu'
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
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('estimationRuleSet.add', 'MDC', 'Add estimation rule sets'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'device-configuration-estimation-add-rule-sets-bulk-selection',
                            router: me.router
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.noItems', 'MDC', 'No estimation rule sets found'),
                            reasons: [
                                Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.empty.list.item1', 'MDC', 'No estimation rule sets have been added yet.'),
                                Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.empty.list.item2', 'MDC', 'Estimation rule sets exist, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('estimationRuleSet.add', 'MDC', 'Add estimation rule sets'),
                                    action: 'addEstimationRuleSet'
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
    },

    updateValidationRuleSet: function (validationRuleSet) {
        this.down('validation-ruleset-view').updateValidationRuleSet(validationRuleSet);
    }
});
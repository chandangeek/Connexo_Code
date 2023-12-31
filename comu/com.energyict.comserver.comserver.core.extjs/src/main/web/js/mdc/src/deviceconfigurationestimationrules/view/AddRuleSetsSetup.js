/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                                Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.empty.list.item3', 'MDC', 'Estimation rule sets exist, but none of them match this device configuration.'),
                                Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.empty.list.item2', 'MDC', 'Matching estimation rule sets exist, but you do not have permission to view them.'),
                                Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.empty.list.item4', 'MDC', 'All matching estimation rule sets have already been added to the device configuration.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('estimationRuleSet.add', 'MDC', 'Add estimation rule sets'),
                                    action: 'addEstimationRuleSet'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'panel',
                            ui: 'medium',
                            style: {
                                paddingLeft: 0
                            },
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
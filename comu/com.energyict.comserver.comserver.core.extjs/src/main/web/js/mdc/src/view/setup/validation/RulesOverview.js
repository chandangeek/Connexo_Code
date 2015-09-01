Ext.define('Mdc.view.setup.validation.RulesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'validation-rules-overview',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.validation.RuleSetsGrid',
        'Mdc.view.setup.validation.RulesGrid',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Mdc.view.setup.validation.RuleSetView'
    ],

    deviceTypeId: null,
    deviceConfigId: null,
    validationRuleSetId: null,

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
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRuleSets', 'MDC', 'Validation rule sets'),

                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'validation-rulesets-grid',
                            itemId: 'grd-validation-rule-sets',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            itemId: 'ctr-no-validation-rule-set',
                            title: Uni.I18n.translate('validation.rulesets.empty.title', 'MDC', 'No validation rule sets found'),
                            reasons: [
                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item1', 'MDC', 'No validation rule sets have been defined yet.'),
                                Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item2', 'MDC', 'Validation rule sets exist, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    text: Uni.I18n.translate('validation.addValidationRuleSets', 'MDC', 'Add validation rule sets'),
                                    privileges : Cfg.privileges.Validation.deviceConfiguration,
                                    href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets/add'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'validation-ruleset-view'
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    updateValidationRuleSet: function (validationRuleSet) {
        var preview = this.down('validation-ruleset-view');
        preview.updateValidationRuleSet(validationRuleSet);
    }
});
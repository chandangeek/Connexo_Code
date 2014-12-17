Ext.define('Mdc.view.setup.validation.AddRuleSets', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'validation-add-rulesets',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.validation.AddRuleSetsGrid',
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
        'Mdc.view.setup.validation.RuleSetView'
    ],

    deviceTypeId: null,
    deviceConfigId: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        deviceTypeId: me.deviceTypeId,
                        deviceConfigurationId: me.deviceConfigId,
                        toggle: 8
                    }
                ]
            }
        ];

        me.content = [
            {
                ui: 'large',
                xtype: 'panel',
                title: Uni.I18n.translate('validation.validationRules', 'MDC', 'Add validation rule sets'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        selectByDefault: false,
                        grid: {
                            xtype: 'validation-add-rulesets-grid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rule sets found'),
                            reasons: [
                                Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rule sets have been added yet.'),
                                Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rule sets exists, but you do not have permission to view them.')
                            ],
                            stepItems: [
                                {
                                    xtype: 'button',
                                    text: Uni.I18n.translate('validation.addValidationRuleSets', 'MDC', 'Add validation rule sets'),
                                    privileges: ['privilege.view.fineTuneValidationConfiguration.onDeviceConfiguration'],
                                    ui: 'action',
                                    href: '#/administration/validation/rulesets/add'
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'validation-ruleset-view',
                            hidden: true
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
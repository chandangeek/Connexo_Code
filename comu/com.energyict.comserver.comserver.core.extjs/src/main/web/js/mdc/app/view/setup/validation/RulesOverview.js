Ext.define('Mdc.view.setup.validation.RulesOverview', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'validation-rules-overview',

    requires: [
        'Uni.view.container.PreviewContainer',
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
                title: Uni.I18n.translate('validation.validationRuleSets', 'MDC', 'Validation rule sets'),

                items: [
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'validation-rulesets-grid',
                            deviceTypeId: me.deviceTypeId,
                            deviceConfigId: me.deviceConfigId
                        },
                        emptyComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<h4>' + Uni.I18n.translate('validation.empty.title', 'MDC', 'No validation rule sets found') + '</h4><br>' +
                                                Uni.I18n.translate('validation.empty.detail', 'MDC', 'There are no validation rule sets. This could be because:') + '<ul>' +
                                                '<li>' + Uni.I18n.translate('validation.empty.list.item1', 'MDC', 'No validation rule sets have been added yet.') + '</li>' +
                                                '<li>' + Uni.I18n.translate('validation.empty.list.item2', 'MDC', 'Validation rule sets exists, but you do not have permission to view them.') + '</li>' +
                                                '</ul>' + Uni.I18n.translate('validation.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            xtype: 'button',
                                            text: Uni.I18n.translate('validation.addValidationRuleSets', 'MDC', 'Add validation rule sets'),
                                            ui: 'action',
                                            href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets/add'
                                        }
                                    ]
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
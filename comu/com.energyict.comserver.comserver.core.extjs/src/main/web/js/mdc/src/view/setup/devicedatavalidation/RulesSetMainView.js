Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceDataValidationRulesSetMainView',
    itemId: 'deviceDataValidationRulesSetMainView',

    device: null,

    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesSetGrid',
        'Mdc.view.setup.devicedatavalidation.RulesSetPreview',
        'Mdc.view.setup.devicedatavalidation.RuleSetVersionPreview',
        'Uni.view.container.PreviewContainer'
    ],
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'dataValidationLink'
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('device.dataValidation.validationConfiguration', 'MDC', 'Validation configuration'),
                items: [
                    {
                        xtype: 'container',
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'panel',
                                itemId: 'dataValidationStatusPanel',
                                ui: 'medium',
                                layout: 'column',
                                title: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'deviceDataValidationStatusField',
                                        columnWidth: 1,
                                        labelAlign: 'left',
                                        fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                        value: Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'deviceDataValidationStateChangeBtn',
                                        privileges:Cfg.privileges.Validation.device,
                                        action: '',
                                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('device.dataValidation.rulesSetGrid.title', 'MDC', 'Validation rule sets'),
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'preview-container',
                                grid: {
                                    xtype: 'deviceDataValidationRulesSetGrid',
                                    mRID: me.device.get('mRID')
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    title: Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.title', 'MDC', 'No validation rule sets found'),
                                    reasons: [
                                        Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item1', 'MDC', 'No validation rule sets have been defined yet.'),
                                        Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item2', 'MDC', 'Validation rule sets exist, but you do not have permission to view them.')
                                    ],
                                    stepItems: [
                                        {
                                            text: Uni.I18n.translate('validation.addRuleSet', 'MDC', 'Add validation rule set'),
                                            ui: 'action',
                                            href: '#/administration/validation/rulesets/add'
                                        }
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'container',
                                    itemId: 'deviceDataValidationRulesSetPreviewCt'
                                }
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});
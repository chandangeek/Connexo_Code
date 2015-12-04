Ext.define('Imt.validation.view.RulesSetMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagePointDataValidationRulesSetMainView',
    itemId: 'usagePointDataValidationRulesSetMainView',

    usagePoint: null,

    requires: [
        'Imt.validation.view.RulesSetGrid',
        'Imt.validation.view.RulesSetPreview',
        'Imt.validation.view.RuleSetVersionPreview',
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
                        xtype: 'usage-point-management-side-menu',
                        itemId: 'usage-point-management-side-menu',
                        router: me.router,
//                        mRID: me.mRID,
                        mRID: me.usagePoint.get('mRID'),
//                        toggleId: 'dataValidationLink'
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('usagepoint.dataValidation.validationConfiguration', 'IMT', 'Validation configuration'),
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
                                        itemId: 'usagePointDataValidationStatusField',
                                        columnWidth: 1,
                                        labelAlign: 'left',
                                        fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                                        value: Uni.I18n.translate('usagepoint.dataValidation.updatingStatus', 'IMT', 'Updating status...')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'usagePointDataValidationStateChangeBtn',
                                        //privileges:Cfg.privileges.Validation.device,
                                        action: '',
                                        //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.title', 'IMT', 'Validation rule sets'),
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'preview-container',
                                grid: {
                                    xtype: 'usagePointDataValidationRulesSetGrid',
                    				mRID: me.usagePoint.get('mRID'),
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    title: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.title', 'IMT', 'No validation rule sets found'),
                                    reasons: [
                                        Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.item1', 'IMT', 'No validation rule sets have been defined yet.'),
                                        Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.emptyCmp.item2', 'IMT', 'Validation rule sets exist, but you do not have permission to view them.')
                                    ],
                                    stepItems: [
                                        {
                                            text: Uni.I18n.translate('validation.addRuleSet', 'IMT', 'Add validation rule set'),
                                            ui: 'action',
                                            href: '#/administration/validation/rulesets/add'
                                        }
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'container',
                                    itemId: 'usagePointDataValidationRulesSetPreviewCt'
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
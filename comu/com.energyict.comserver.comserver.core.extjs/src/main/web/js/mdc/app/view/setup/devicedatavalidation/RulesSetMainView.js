Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceDataValidationRulesSetMainView',
    itemId: 'deviceDataValidationRulesSetMainView',
    mRID: null,
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesSetGrid',
        'Mdc.view.setup.devicedatavalidation.RulesSetPreview',
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
                        mRID: me.mRID,
                        toggle: 7
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('device.dataValidation', 'MDC', 'Data validation'),
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
                                title: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status'),
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'deviceDataValidationStatusField',
                                        columnWidth: 1,
                                        labelAlign: 'left',
                                        fieldLabel: Uni.I18n.translate('device.dataValidation.statusSection.title', 'MDC', 'Status'),
                                        value: Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'deviceDataValidationStateChangeBtn',
                                        text: Uni.I18n.translate('device.dataValidation.updatingStatus', 'MDC', 'Updating status...'),
                                        action: ''
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
                                    mRID: me.mRID
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    title: Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.title', 'MDC', 'No validation rule sets found'),
                                    reasons: [
                                        Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item1', 'MDC', 'No validation rule sets have been assigned to this device.'),
                                        Uni.I18n.translate('device.dataValidation.rulesSetGrid.emptyCmp.item2', 'MDC', 'Validation rule sets have been assigned, but you do not have permission to view them.')
                                    ],
                                    stepItems: []
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
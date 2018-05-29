/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulesSetMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimationCfgRulesSetMainView',
    itemId: 'estimationCfgRulesSetMainView',

    device: null,
    router: null,

    requires: [
        'Imt.purpose.view.summary.estimation.EstimationRulesSetGrid',
        'Imt.purpose.view.summary.estimation.EstimationRulesSetPreview',
        'Uni.view.container.PreviewContainer',
        //'Mdc.view.setup.device.DeviceMenu'
    ],
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                items: [
                    {
                        xtype: 'container',
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'panel',
                                itemId: 'estimationCfgStatusPanel',
                                ui: 'medium',
                                layout: 'column',
                                title: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'estimationCfgStatusField',
                                        columnWidth: 1,
                                        labelAlign: 'left',
                                        fieldLabel: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                                        value: Uni.I18n.translate('estimationPurpose.updatingStatus', 'IMT', 'Updating status...')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'estimationCfgStateChangeBtn',
                                        action: '',
                                        // privileges: Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfigurationOnDevice,
                                        //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.estimationActions
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('general.estimationRuleSets', 'IMT', 'Estimation rule sets'),
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'preview-container',
                                grid: {
                                    xtype: 'estimationCfgRulesSetGrid',
                                    itemId: 'purpose-estimation-rules-set-grid'
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'no-estimation-rule-sets-panel',
                                    title: Uni.I18n.translate('estimationPurpose.rulesSetGrid.emptyCmp.title', 'IMT', 'No estimation rule sets found'),
                                    reasons: [
                                        Uni.I18n.translate('estimationPurpose.rulesSetGrid.emptyCmp.item1', 'IMT', 'No estimation rule sets have been defined yet'),
                                        Uni.I18n.translate('estimationPurpose.rulesSetGrid.emptyCmp.item2', 'IMT', 'Estimation rule sets exist, but you do not have permission to view them')
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'container',
                                    itemId: 'estimationCfgRulesSetPreviewCt'
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
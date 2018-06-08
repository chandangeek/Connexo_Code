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
        'Uni.view.container.PreviewContainer'
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
                                        htmlEncode: false,
                                        valueToRaw: function (v) {
                                            return v;
                                        },
                                        renderer: function (value) {
                                            var status = 'Updating status ...',
                                                icon = '';

                                            switch (value) {
                                                case true:
                                                    status = Uni.I18n.translate('purpose.validation.status.active', 'IMT', 'Active');
                                                    icon = '<span class="icon-checkmark-circle" style="color: #33CC33; margin-left: 10px"></span>';
                                                    break;
                                                case false:
                                                    status = Uni.I18n.translate('purpose.validation.status.inactive', 'IMT', 'Inactive');
                                                    icon = '<span class="icon-blocked" style="color: #eb5642; margin-left: 10px"></span>';
                                                    break;
                                            }
                                            return status + icon
                                        }
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'estimationCfgStateChangeBtn',
                                        disabled: false,
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
                                    xtype: 'estimationCfgRulesSetGrid'
                                    //itemId: 'purpose-estimation-rules-set-grid',
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'no-estimation-rule-sets',
                                    title: Uni.I18n.translate('estimationrulesets.empty.title', 'IMT', 'No estimation rule sets found'),
                                    reasons: [
                                        Uni.I18n.translate('estimationrulesets.reason1', 'IMT', 'No estimation rule sets have been defined yet.'),
                                        Uni.I18n.translate('estimationrulesets.reason2', 'IMT', 'Estimation rule sets exist, but you do not have permission to view them.')
                                    ],
                                    stepItems: [
                                        {
                                            itemId: 'estimation-rule-set-to-purpose-empty-msg-btn',
                                            text: Uni.I18n.translate('estimation.addRuleSet', 'IMT', 'Add estimation rule set'),
                                            //privileges: Cfg.privileges.Validation.admin,
                                            href: me.router.getRoute('administration/estimationrulesets/addruleset').buildUrl()
                                        }
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
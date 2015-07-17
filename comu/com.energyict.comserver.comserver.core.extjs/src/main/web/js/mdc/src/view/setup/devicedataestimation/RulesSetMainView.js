Ext.define('Mdc.view.setup.devicedataestimation.RulesSetMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceDataEstimationRulesSetMainView',

    device: null,
    router: null,

    requires: [
        'Mdc.view.setup.devicedataestimation.RulesSetGrid',
        'Mdc.view.setup.devicedataestimation.RulesSetPreview',
        'Uni.view.container.PreviewContainer',
        'Mdc.view.setup.device.DeviceMenu'
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
                        toggleId: 'dataEstimationLink'
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('estimationDevice.dataEstimation', 'MDC', 'Data estimation'),
                items: [
                    {
                        xtype: 'container',
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'panel',
                                itemId: 'dataEstimationStatusPanel',
                                ui: 'medium',
                                layout: 'column',
                                title: Uni.I18n.translate('estimationDeviceConfigurations.status', 'MDC', 'Status'),
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'deviceDataEstimationStatusField',
                                        columnWidth: 1,
                                        labelAlign: 'left',
                                        fieldLabel: Uni.I18n.translate('estimationDeviceConfigurations.status', 'MDC', 'Status'),
                                        value: Uni.I18n.translate('estimationDevice.updatingStatus', 'MDC', 'Updating status...')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'deviceDataEstimationStateChangeBtn',
                                        action: '',
                                        privileges: Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfigurationOnDevice,
                                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.estimationActions
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        ui: 'medium',
                        title: Uni.I18n.translate('estimationDevice.estimationRuleSets', 'MDC', 'Estimation rule sets'),
                        margin: '0 0 0 -10',
                        items: [
                            {
                                xtype: 'preview-container',
                                grid: {
                                    xtype: 'deviceDataEstimationRulesSetGrid',
                                    itemId: 'device-data-estimation-rules-set-grid',
                                    mRID: me.device.get('mRID')
                                },
                                emptyComponent: {
                                    xtype: 'no-items-found-panel',
                                    itemId: 'no-estimation-rule-sets-panel',
                                    title: Uni.I18n.translate('estimationDevice.rulesSetGrid.emptyCmp.title', 'MDC', 'No estimation rule sets found'),
                                    reasons: [
                                        Uni.I18n.translate('estimationDevice.rulesSetGrid.emptyCmp.item1', 'MDC', 'No estimation rule sets have been defined yet'),
                                        Uni.I18n.translate('estimationDevice.rulesSetGrid.emptyCmp.item2', 'MDC', 'Estimation rule sets exist, but you do not have permission to view them')
                                    ]
                                },
                                previewComponent: {
                                    xtype: 'container',
                                    itemId: 'deviceDataEstimationRulesSetPreviewCt'
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
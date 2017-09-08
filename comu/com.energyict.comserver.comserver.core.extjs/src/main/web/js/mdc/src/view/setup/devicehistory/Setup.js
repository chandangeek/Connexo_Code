/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicehistory.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-history-setup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicehistory.LifeCycle',
        'Mdc.view.setup.devicehistory.MeterActivations',
        'Uni.view.container.EmptyGridContainer',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.devicehistory.IssueAlarmFilter',
        'Mdc.view.setup.devicehistory.IssueAlarmGrid',
        'Mdc.view.setup.devicehistory.IssueAlarmPreview'
    ],

    router: null,
    device: null,
    activeTab: null,

    initComponent: function () {
        var me = this;

        me.side = {
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceMenu',
                    itemId: 'device-history-side-menu',
                    device: me.device
                }
            ]
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.history', 'MDC', 'History'),
            itemId: 'history-panel',
            items: [
                {
                    xtype: 'tabpanel',
                    margin: '20 0 0 0',
                    itemId: 'device-history-tab-panel',
                    activeTab: me.activeTab,
                    width: '100%',
                    items: [
                        {
                            title: Uni.I18n.translate('general.deviceLifeCycle', 'MDC', 'Device life cycle'),
                            padding: '8 16 16 0',
                            itemId: 'device-history-life-cycle-tab'
                        },
                        {
                            title: Uni.I18n.translate('general.firmware', 'MDC', 'Firmware'),
                            padding: '8 16 16 0',
                            itemId: 'device-history-firmware-tab'
                        },
                        {
                            title: Uni.I18n.translate('general.meterActivation', 'MDC', 'Meter activation'),
                            padding: '8 16 16 0',
                            itemId: 'device-history-meter-activations-tab',
                            items: {
                                xtype: 'emptygridcontainer',
                                grid: {
                                    xtype: 'device-history-meter-activations-tab',
                                    itemId: 'device-history-meter-activations-grid',
                                    device: me.device,
                                    router: me.router
                                },
                                emptyComponent: {
                                    xtype: 'form',
                                    items: [
                                        {
                                            xtype: 'uni-form-empty-message',
                                            text: Uni.I18n.translate('general.device.noMeterActivations', 'MDC', 'No meter activations for this device')
                                        }
                                    ]
                                }
                            },

                            listeners: {
                                activate: me.controller.showMeterActivations,
                                scope: me.controller
                            }
                        },
                        {
                            title: Uni.I18n.translate('general.issuesAndAlarms', 'MDC', 'Issues and Alarms'),
                            padding: '8 16 16 0',
                            itemId: 'device-history-issues-alarms-tab',
                            items: {
                                xtype: 'contentcontainer',
                                content: [
                                    {
                                        xtype: 'issues-alarm-filter',
                                        itemId: 'issues-alarm-filter'
                                    },
                                    {
                                        store: 'Mdc.store.device.IssuesAlarms',
                                        xtype: 'issues-alarms-grid',
                                        itemId: 'issues-alarms-grid'
                                    },
                                    {
                                        xtype: 'issues-alarms-preview',
                                        itemId: 'issues-alarms-preview',
                                        fieldxtype: 'displayfield'
                                    }

                                ],
                                grid1: {
                                    xtype: 'device-history-meter-activations-tab',
                                    itemId: 'device-history-meter-activations-grid',
                                    device: me.device,
                                    router: me.router
                                },
                                emptyComponent1: {
                                    xtype: 'form',
                                    items: [
                                        {
                                            xtype: 'uni-form-empty-message',
                                            text: Uni.I18n.translate('general.device.noMeterActivations', 'MDC', 'No meter activations for this device')
                                        }
                                    ]
                                }
                            },
                        },
                    ]
                }
            ]
        };

        me.callParent(arguments);
    },

    loadCustomAttributeSets: function (customAttributeSetsStore) {
        var me = this;

        Ext.suspendLayouts();
        customAttributeSetsStore.each(function (customAttributeSet) {
            if (customAttributeSet.get('timesliced')) {
                me.down('#device-history-tab-panel').add(
                    {
                        title: customAttributeSet.get('name'),
                        itemId: 'custom-attribute-set-' +  customAttributeSet.get('id'),
                        customAttributeSetId: customAttributeSet.get('id')
                    }
                )
            }
        });
        Ext.resumeLayouts(true);
    }
});

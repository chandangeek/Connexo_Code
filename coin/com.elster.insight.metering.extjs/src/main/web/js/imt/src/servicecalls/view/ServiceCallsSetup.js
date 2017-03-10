/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.servicecalls.view.ServiceCallsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.service-calls-setup',
    itemId: 'serviceCallsSetup',
    usagePointId: null,
    activeTab: 0,
    filterDefault: {},

    requires: [
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Scs.view.object.RunningServiceCallsPreviewContainer',
        'Scs.view.object.HistoryServiceCallsPreviewContainer',
        'Scs.view.ServiceCallFilter'
    ],

    stores: [
        'Scs.store.object.ServiceCallHistory',
        'Scs.store.object.RunningServiceCalls',
        'Scs.store.ServiceCallTypes',
        'Scs.store.States'
    ],
    usagePoint: null,

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
                        usagePointId: me.usagePointId,
                        usagePoint: me.usagePoint
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'tabpanel',
                itemId: 'object-service-calls-tab-panel',
                title: Uni.I18n.translate('general.serviceCalls', 'IMT', 'Service calls'),
                ui: 'large',
                activeTab: me.activeTab,
                usagePointId: me.usagePointId,
                items: [
                    {
                        title: Uni.I18n.translate('servicecalls.runningServiceCalls', 'IMT', 'Running service calls'),
                        itemId: 'running-service-calls-tab',
                        items: [
                            {
                                xtype: 'running-service-call-preview-container',
                                store: 'Scs.store.object.RunningServiceCalls'
                            }
                        ]
                    },
                    {
                        title: Uni.I18n.translate('general.history', 'IMT', 'History'),
                        itemId: 'history-service-calls-tab'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },

    addHistoryGrid: function (filterDefault) {
        var me = this;

        me.suspendLayouts();
        me.down('#history-service-calls-tab').add(
            {
                xtype: 'service-call-filter',
                store: 'Scs.store.object.ServiceCallHistory',
                modDateHidden: true,
                filterDefault: filterDefault
            },
            {
                xtype: 'history-service-call-preview-container',
                store: 'Scs.store.object.ServiceCallHistory'
            });

        me.resumeLayouts(true);

        me.doLayout();
    }
});




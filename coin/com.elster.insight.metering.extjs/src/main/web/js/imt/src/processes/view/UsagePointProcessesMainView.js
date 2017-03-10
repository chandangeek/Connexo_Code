/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.UsagePointProcessesMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-processes-main-view',
    overflowY: 'auto',
    requires: [
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Bpm.monitorprocesses.view.MonitorProcessesMainView'
    ],
    router: null,
    usagePoint: null,
    properties: null,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'usage-point-management-side-menu',
                                itemId: 'usage-point-management-side-menu',
                                router: me.router,
                                usagePoint: me.usagePoint
                            }
                        ]
                    }
                ]
            }
        ];
        me.content = [
            {
                xtype: 'bpm-monitor-processes-main-view',
                ui: 'large',
                itemId: 'usage-point-monitor-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});
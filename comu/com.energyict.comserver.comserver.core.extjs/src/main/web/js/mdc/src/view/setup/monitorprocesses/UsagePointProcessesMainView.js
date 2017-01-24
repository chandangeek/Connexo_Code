Ext.define('Mdc.view.setup.monitorprocesses.UsagePointProcessesMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usage-point-processes-main-view',
    overflowY: 'auto',
    requires: [
        'Bpm.monitorprocesses.view.MonitorProcessesMainView'
    ],
    router: null,
    deviceId: null,
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
                                deviceId: me.deviceId
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
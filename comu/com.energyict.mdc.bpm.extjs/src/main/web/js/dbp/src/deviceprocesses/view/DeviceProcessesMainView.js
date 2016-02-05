Ext.define('Dbp.deviceprocesses.view.DeviceProcessesMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-device-processes-main-view',
    overflowY: 'auto',
    requires: [
        'Bpm.monitorprocesses.view.MonitorProcessesMainView'
    ],
    device: null,
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
                                xtype: 'deviceMenu',
                                itemId: 'steps-Menu',
                                device: me.device,
                                toggleId: 'processesLink'
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
                itemId: 'device-monitor-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});
Ext.define('Dbp.monitorprocesses.view.UsagePointProcessesMainView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.dbp-usage-point-processes-main-view',
    overflowY: 'auto',
    requires: [
        'Bpm.monitorprocesses.view.MonitorProcessesMainView'
    ],
    router: null,
    mRID: null,
    properties: null,
    sidePanel: null,

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
                                xtype: me.sidePanel,
                                //itemId: 'usage-point-management-side-menu',
                                router: me.router,
                                mRID: me.mRID
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
Ext.define('Mdc.usagepointmanagement.view.StartProcess', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.mdc-usage-point-start-process-view',
    requires: [
        'Bpm.startprocess.view.StartProcess'
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
                                xtype: 'usage-point-management-side-menu',
                                itemId: 'usage-point-management-side-menu',
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
                xtype: 'bpm-start-processes-panel',
                ui: 'large',
                itemId: 'usage-point-start-processes-panel',
                properties: me.properties
            }
        ];
        me.callParent(arguments);
    }
});


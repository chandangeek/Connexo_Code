Ext.define('Mdc.view.setup.devicetopology.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTopologySetup',
    itemId: 'deviceTopologySetup',
    router: null,
    device: null,

    requires: [
        'Mdc.view.setup.devicetopology.Grid',
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.devicechannels.DeviceTopologiesTopFilter'
    ],

    stores: [
        'Mdc.store.TopologyOfDevice'
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
                        toggleId: 'topologyLink'
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceTopologyGrid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceCommunicationTopology.empty.title', 'MDC', 'No slave devices found'),
                        reasons: [
                            Uni.I18n.translate('deviceCommunicationTopology.empty.list.item1', 'MDC', 'The gateway contains no slave devices.'),
                            Uni.I18n.translate('deviceCommunicationTopology.empty.list.item2', 'MDC', 'No slave devices comply with the filter.')
                        ]
                    }
                }
            ],
            dockedItems: [
                {
                    dock: 'top',
                    xtype: 'mdc-view-setup-devicechannels-topologiestopfilter'
                }
            ]
        };
        me.callParent(arguments);
    }
});
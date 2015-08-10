Ext.define('Mdc.view.setup.devicetopology.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTopologySetup',
    itemId: 'deviceTopologySetup',
    router: null,
    device: null,

    requires: [
        'Mdc.view.setup.devicetopology.Grid',
        'Mdc.view.setup.devicetopology.SideFilter',
        'Mdc.view.setup.device.DeviceMenu'
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
            },
            {
                xtype: 'topology-side-filter'
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceCommunicationTopology.topologyTitle', 'MDC', 'Communication topology'),
            items: [
                {
                    title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
                    xtype: 'filter-top-panel',
                    itemId: 'topFilterDeviceTopology',
                    margin: '0 0 20 0',
                    name: 'filter',
                    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None'),
                    hidden: true
                },
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
                            Uni.I18n.translate('deviceCommunicationTopology.empty.list.item2', 'MDC', 'The filter is too narrow.')
                        ]
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});
Ext.define('Fwc.devicefirmware.view.Log', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-firmware-log',

    requires: [
        'Fwc.devicefirmware.view.LogGrid',
        'Fwc.devicefirmware.view.LogPreview',
        'Mdc.view.setup.device.DeviceMenu'
    ],

    router: null,
    device: null,

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'deviceMenu',
            router: me.router,
            device: me.device
        };

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceFirmware.logTitle', 'FWC', 'Meter firmware upgrade log to version'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'device-firmware-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('deviceLifeCycleStates.empty.title', 'DLC', 'No states found'),
                        reasons: [
                            Uni.I18n.translate('deviceLifeCycleStates.empty.list.item1', 'DLC', 'No states have been added yet')
                        ]
                    },
                    previewComponent: {
                        xtype: 'device-firmware-log-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
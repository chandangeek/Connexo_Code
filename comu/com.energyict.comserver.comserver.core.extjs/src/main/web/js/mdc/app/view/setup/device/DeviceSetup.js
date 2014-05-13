Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    deviceTypeId: null,
    itemId: 'deviceSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.device.DeviceCommunicationTopologyPanel',
        'Mdc.view.setup.device.DeviceGeneralInformationPanel',
        'Mdc.view.setup.device.DeviceOpenIssuesPanel'
    ],

    content: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('devicesetup.deviceConfigurations', 'MDC', 'deviceName') + '</h1>',
                    itemId: 'deviceSetupTitle',
                    margins: '10 10 0 10'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    itemId: 'DeviceContainer',
                    layout: {
                        type: 'column'
                    },
                    width: '100%'
                }
            ]}
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceMenu',
                itemId: 'stepsMenu',
                deviceId: this.deviceId,
                toggle: 0
            }
        ];
        this.callParent(arguments);
        this.down('#DeviceContainer').add(
            {
                xtype: 'deviceGeneralInformationPanel',
                deviceId: this.deviceId,
                columnWidth: 0.50
            }
        );
        this.down('#DeviceContainer').add(
            {
                xtype: 'deviceCommunicationTopologyPanel',
                deviceId: this.deviceId,
                columnWidth: 0.50
            }
        );
        this.down('#DeviceContainer').add(
            {
                xtype: 'deviceOpenIssuesPanel',
                deviceId: this.deviceId,
                columnWidth: 0.50
            }
        );
    },

    addSlaveDevice: function (mRID, id) {
        var me = this;
        var slaveDevices = me.down('#slaveDevicesContainer');
        slaveDevices.add(
            {
                xtype: 'component',
                cls: 'x-form-display-field',
                autoEl: {
                    tag: 'a',
                    href: '#/setup/devices/' + id,
                    html: mRID
                }
            });
    }
});
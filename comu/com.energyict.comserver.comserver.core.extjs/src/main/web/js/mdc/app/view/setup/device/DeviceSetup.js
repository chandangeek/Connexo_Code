Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    deviceTypeId: null,
    mRID: null,
    itemId: 'deviceSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.device.DeviceCommunicationTopologyPanel',
        'Mdc.view.setup.device.DeviceGeneralInformationPanel',
        'Mdc.view.setup.device.DeviceOpenIssuesPanel'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'deviceSetupPanel',
            title: Uni.I18n.translate('devicesetup.deviceConfigurations', 'MDC', 'deviceName'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    itemId: 'DeviceContainer',
                    layout: {
                        type: 'column'
                    },
                    width: '100%',
                    defaults: {
                        margin: '0 16 16 0',
                        columnWidth: 0.5
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [{
                    xtype: 'deviceMenu',
                    itemId: 'stepsMenu',
                    deviceId: this.deviceId,
                    toggle: 0
                }]
            }
        ];
        this.callParent(arguments);
        this.down('#DeviceContainer').add(
            {
                xtype: 'deviceGeneralInformationPanel',
                deviceId: this.deviceId
            }
        );
        this.down('#DeviceContainer').add(
            {
                xtype: 'deviceCommunicationTopologyPanel',
                deviceId: this.deviceId
            }
        );
        this.down('#DeviceContainer').add(
            {
                xtype: 'deviceOpenIssuesPanel',
                deviceId: this.deviceId,
                mRID: this.mRID
            }
        );
    },

    addSlaveDevice: function (mRID) {
        var me = this;
        var slaveDevices = me.down('#slaveDevicesContainer');
        slaveDevices.add(
            {
                xtype: 'component',
                cls: 'x-form-display-field',
                autoEl: {
                    tag: 'a',
                    href: '#/devices/' + mRID,
                    html: mRID
                }
            });
    }
});
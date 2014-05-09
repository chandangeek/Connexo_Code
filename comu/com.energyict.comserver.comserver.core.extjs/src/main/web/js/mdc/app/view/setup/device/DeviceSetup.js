Ext.define('Mdc.view.setup.device.DeviceSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSetup',
    deviceTypeId: null,
    itemId: 'deviceSetup',
    requires: [
        'Uni.view.breadcrumb.Trail',
        'Mdc.view.setup.device.DeviceMenu'
    ],
//    border: 0,
//    region: 'center',

    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h2>' + Uni.I18n.translate('devicesetup.deviceConfigurations', 'MDC', 'deviceName') + '</h2>',
                    itemId: 'deviceSetupTitle',
                    margins: '10 10 0 10'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    itemId: 'DeviceContainer'
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
                deviceId: this.deviceId
            }
        );
    }
})
;



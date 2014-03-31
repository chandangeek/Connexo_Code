Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationsSetup',
    autoScroll: true,
    deviceTypeId: null,
    itemId: 'deviceConfigurationsSetup',
    requires: [
        'Uni.view.breadcrumb.Trail',
        'Mdc.view.setup.devicetype.DeviceTypeMenu'
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
                    html: '<h1>' + Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations') + '</h1>',
                    margins: '10 10 10 10'
                },
                {
                    xtype: 'container',
                    itemId: 'DeviceConfigurationsGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'deviceConfigurationPreview'
                }
            ]}
    ],


    initComponent: function () {
        this.side = [
            {
                xtype: 'deviceTypeMenu',
                itemId: 'stepsMenu',
                deviceTypeId: this.deviceTypeId,
                toggle: 4
            }
        ];
        this.callParent(arguments);
        this.down('#DeviceConfigurationsGridContainer').add(
            {
                xtype: 'deviceConfigurationsGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
    }
})
;



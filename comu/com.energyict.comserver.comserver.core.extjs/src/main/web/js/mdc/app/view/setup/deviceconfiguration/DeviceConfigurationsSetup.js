Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationsSetup',
    deviceTypeId: null,
    itemId: 'deviceConfigurationsSetup',

    requires: [
        'Mdc.view.setup.devicetype.DeviceTypeMenu'
    ],


    content: [
//        {
//            ui: 'large',
//            xtype: 'panel',
//            title: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),
//            items: [
//                {
//                    xtype: 'container',
//                    itemId: 'DeviceConfigurationsGridContainer'
//                },
//                {
//                    xtype: 'deviceConfigurationPreview'
//                }
//            ]
//        }
    ],


    initComponent: function () {
        this.side = [

            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeMenu',
                        itemId: 'stepsMenu',
                        deviceTypeId: this.deviceTypeId,
                        toggle: 4
                    }
                ]
            }
        ];
        this.content =  [
            {
                ui: 'large',
                xtype: 'panel',
                itemId: 'connectionMethodSetupPanel',
                title: Uni.I18n.translate('deviceconfiguration.deviceConfigurations', 'MDC', 'Device configurations'),

                items: [
                    {
                        xtype: 'preview-container',
                        itemId: 'previewContainer',
                        grid: {
                            xtype: 'deviceConfigurationsGrid',
                            deviceTypeId: this.deviceTypeId
                        },
                        emptyComponent: {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'left'
                            },
                            minHeight: 20,
                            items: [
                                {
                                    xtype: 'image',
                                    margin: '0 10 0 0',
                                    src: "../mdc/resources/images/information.png",
                                    height: 20,
                                    width: 20
                                },
                                {
                                    xtype: 'container',
                                    items: [
                                        {
                                            xtype: 'component',
                                            html: '<h4>' + Uni.I18n.translate('deviceConfiguration.empty.title', 'MDC', 'No device configurations found') + '</h4><br>' +
                                                Uni.I18n.translate('deviceConfiguration.empty.detail', 'MDC', 'There are no device configurations. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                                Uni.I18n.translate('deviceConfiguration.empty.list.item1', 'MDC', 'No device configurations have been defined yet.') + '</li></lv><br>' +
                                                Uni.I18n.translate('deviceConfiguration.empty.steps', 'MDC', 'Possible steps:')
                                        },
                                        {
                                            text: Uni.I18n.translate('deviceConfiguration.addDeviceConfiguration', 'MDC', 'Add outbound connection method'),
                                            itemId: 'createDeviceConfigurationButton',
                                            xtype: 'button',
                                            action: 'createDeviceConfiguration'
                                        }
                                    ]
                                }
                            ]
                        },
                        previewComponent: {
                            xtype: 'deviceConfigurationPreview'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});
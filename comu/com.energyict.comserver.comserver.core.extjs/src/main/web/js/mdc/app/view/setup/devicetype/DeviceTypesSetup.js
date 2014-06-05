Ext.define('Mdc.view.setup.devicetype.DeviceTypesSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceTypesSetup',
    itemId: 'deviceTypeSetup',

    requires: [
        'Mdc.view.setup.devicetype.DeviceTypesGrid',
        'Mdc.view.setup.devicetype.DeviceTypePreview',
        'Uni.view.container.PreviewContainer'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('devicetype.deviceTypes', 'MDC', 'Device types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'deviceTypesGrid'
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
                                src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                                height: 20,
                                width: 20
                            },
                            {
                                xtype: 'container',
                                items: [
                                    {
                                        xtype: 'component',
                                        html: '<b>' + Uni.I18n.translate('deviceType.empty.title', 'MDC', 'No device types found') + '</b><br>' +
                                            Uni.I18n.translate('deviceType.empty.detail', 'MDC', 'There are no device types. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                            Uni.I18n.translate('deviceType.empty.list.item1', 'MDC', 'No device types have been created yet.') + '</li></lv><br>' +
                                            Uni.I18n.translate('deviceType.empty.steps', 'MDC', 'Possible steps:')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '10 0 0 0',
                                        text: Uni.I18n.translate('deviceType.add', 'MDC', 'Add device type'),
                                        action: 'createDeviceType'
                                    }
                                ]
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'deviceTypePreview'
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});



Ext.define('Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceRegisterConfigurationPreview',
    itemId: 'deviceRegisterConfigurationPreview',
    requires: [
        'Mdc.view.setup.deviceregisterconfiguration.DeviceRegisterConfigurationActionMenu'
    ],
    frame: true,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceRegisterConfigurationActionMenu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            itemId: 'deviceRegisterConfigurationPreviewForm',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.name', 'MDC', 'Name'),
                            name: 'name'
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.obiscode', 'MDC', 'OBIS code'),
                            name: 'obisCode'
                        }
                    ]
                }
            ]
        }
    ]

});



Ext.define('Imt.devicemanagement.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-management-setup',
    itemId: 'device-management-setup',
    requires: [
        'Imt.devicemanagement.view.DeviceSideMenu',
        'Imt.devicemanagement.view.DeviceAttributesFormMain'
    ],
    router: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'deviceSetupPanel',
            layout: {
                type: 'fit',
//                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
//        panel.title = me.router.getRoute().getTitle();
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-management-side-menu',
                        itemId: 'device-management-side-menu',
                        router: me.router,
 //                       mRID: me.mRID
                    }
                ]
            }
        ];
        this.callParent(arguments);

        me.down('#deviceSetupPanel').add(
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox'
                },
                defaults: {
                    style: {
                        marginRight: '20px',
                        padding: '20px'
                    },
                    flex: 1
                },
                items: [
                    {
                        xtype: 'panel',
                        title: Uni.I18n.translate('deviceManagement.deviceGeneralAttributes', 'IMT', 'Device Attributes'),
                        ui: 'tile',
                        itemId: 'device-attributes-panel',
                        router: me.router
                    }
                ]
            }
        );
    }
});
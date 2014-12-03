Ext.define('Mdc.view.setup.deviceregisterconfiguration.TabbedDeviceRegisterView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceRegisterView',
    itemId: 'tabbedDeviceRegisterView',
    content: [
        {
            xtype: 'component',
            itemId: 'deviceRegisterDetailTitle'
        },
        {
            xtype: 'tabpanel',
            itemId: 'registerTabPanel',
            items: [
                {
                    title: 'Specifications',
                    itemId: 'register-specifications'
                },
                {
                    title: 'Data',
                    itemId: 'register-data'
                }]
        }
    ],
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideRegisterPanel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        ui: 'medium',
                        items: [
                            {
                                xtype: 'deviceMenu',
                                itemId: 'stepsMenu',
                                device: me.device,
                                registerId: me.registerId,
                                toggleId: 'registersLink'
                            }
                        ]
                    },
                    {
                        xtype: 'deviceRegisterDataSideFilter',
                        itemId: 'registerFilter',
                        hidden: true
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});

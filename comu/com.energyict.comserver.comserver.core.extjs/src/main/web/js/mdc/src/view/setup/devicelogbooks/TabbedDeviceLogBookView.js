Ext.define('Mdc.view.setup.devicelogbooks.TabbedDeviceLogBookView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tabbedDeviceLogBookView',
    itemId: 'tabbedDeviceLogBookView',
    content: [
        {
            xtype: 'component',
            itemId: 'deviceLogBookDetailTitle'
        },
        {
            xtype: 'tabpanel',
            itemId: 'logBookTabPanel',
            items: [
                {
                    title: 'Specifications',
                    itemId: 'logBook-specifications'
                },
                {
                    title: 'Data',
                    itemId: 'logBook-data'
                }
            ]
        }
    ],
    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                itemId: 'sideLogBookPanel',
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
//                                registerId: me.registerId,
                                toggleId: 'logbooksLink'
                            }
                        ]
                    },
                    {
                        xtype: 'deviceLogbookDataSideFilter',
                        itemId: 'logBookFilter',
                        hidden: true
                    }
                ]
            }

        ];
        me.callParent(arguments);
    }
});

Ext.define('Mdc.view.setup.devicegroup.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'add-devicegroup-browse',
    itemId: 'add-devicegroup-browse',

    requires: [
        'Mdc.view.setup.devicegroup.Navigation',
        'Mdc.view.setup.devicegroup.Wizard',
    ],

    side: {
        itemId: 'devicegroupaddpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'devicegroupaddnavigation',
                xtype: 'devicegroup-add-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'adddevicegroup-wizard',
            itemId: 'adddevicegroupwizard'
        }
    ]
});

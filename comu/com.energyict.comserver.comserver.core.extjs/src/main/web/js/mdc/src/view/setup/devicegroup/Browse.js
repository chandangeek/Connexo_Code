Ext.define('Mdc.view.setup.devicegroup.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'add-devicegroup-browse',
    itemId: 'add-devicegroup-browse',
    requires: [
        'Mdc.view.setup.devicegroup.Navigation',
        'Mdc.view.setup.devicegroup.Wizard',
        'Mdc.view.setup.devicesearch.DevicesSideFilter'
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
            },
            {
                xtype: 'mdc-search-results-side-filter',
                itemId: 'addDeviceGroupSideFilter'
            }
        ]
    },

    content: [
        {
            xtype: 'adddevicegroup-wizard',
            itemId: 'adddevicegroupwizard',
            defaults: {
                cls: 'content-wrapper'
            }
        }
    ]
});

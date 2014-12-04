Ext.define('Yfn.view.setup.generatereport.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'generatereport-browse',
    itemId: 'generatereport-browse',
    requires: [
        'Yfn.view.setup.generatereport.Navigation',
        'Yfn.view.setup.generatereport.Wizard'
    ],

    side: {
        itemId: 'generatereportpanel',
        xtype: 'panel',
        ui: 'medium',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                itemId: 'generatereportnavigation',
                xtype: 'generatereport-navigation'
            }
        ]
    },

    content: [
        {
            xtype: 'generatereport-wizard',
            itemId: 'generatereportwizard',
            layout:'fit',
            defaults: {
                cls: 'content-wrapper'
            }
        }
    ]
});

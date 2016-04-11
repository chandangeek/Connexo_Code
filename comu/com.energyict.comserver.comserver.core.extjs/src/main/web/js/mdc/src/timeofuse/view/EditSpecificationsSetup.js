Ext.define('Mdc.timeofuse.view.EditSpecificationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-devicetype-edit-specs-setup',
    overflowY: true,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.timeofuse.view.EditSpecificationsForm'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('timeofuse.editToUSpecifications', 'MDC', 'Edit time of use specifications'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'tou-devicetype-edit-specs-form'
                }
            ]
        }
    ],

    side: [
        {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceTypeSideMenu',
                    itemId: 'deviceTypeSideMenu',
                    deviceTypeId: this.deviceTypeId,
                    toggle: 0
                }
            ]
        }
    ]


});

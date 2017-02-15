/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.Browse', {
    extend: 'Uni.view.container.ContentContainer',
    xtype: 'add-devicegroup-browse',

    requires: [
        'Mdc.view.setup.devicegroup.Navigation',
        'Mdc.view.setup.devicegroup.Wizard'
    ],

    router: null,
    returnLink: null,
    isEdit: false,
    service: null,

    initComponent: function () {
        var me = this;

        me.side = {
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
                    xtype: 'devicegroup-add-navigation',
                    isEdit: me.isEdit,
                    title: me.isEdit
                        ? ' '
                        : Uni.I18n.translate('devicegroup.wizardMenu', 'MDC', 'Add device group')
                }
            ]
        };

        me.content = [
            {
                xtype: 'adddevicegroup-wizard',
                itemId: 'adddevicegroupwizard',
                isEdit: me.isEdit,
                router: me.router,
                service: me.service,
                returnLink: me.returnLink
            }
        ];

        me.callParent(arguments);
    }
});

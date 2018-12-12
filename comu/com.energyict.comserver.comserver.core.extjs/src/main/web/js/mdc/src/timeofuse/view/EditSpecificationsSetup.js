/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.EditSpecificationsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.tou-devicetype-edit-specs-setup',
    overflowY: true,
    deviceTypeId: null,

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.timeofuse.view.EditSpecificationsForm'
    ],

    initComponent: function () {
        var me = this;

        me.content = [
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
                        xtype: 'tou-devicetype-edit-specs-form',
                        itemId: 'tou-devicetype-edit-specs-form',
                        deviceTypeId: me.deviceTypeId
                    }
                ]
            }
        ];


        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: me.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});

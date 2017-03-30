/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.Specifications', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tou-specifications-preview-panel',
    frame: false,
    timeOfUseSupported: null,
    requires: [
        'Mdc.timeofuse.view.SpecificationsForm',
        'Mdc.timeofuse.view.SpecificationsActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.admin,
                disabled: !me.timeOfUseSupported,
                itemId: 'touSpecificationsButton',
                menu: {
                    xtype: 'tou-spec-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'tou-devicetype-specifications-form',
            itemId: 'tou-devicetype-specifications-form'
        };
        me.callParent(arguments);
    }

});
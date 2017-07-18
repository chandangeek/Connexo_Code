/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tou-preview-panel',
    frame: true,
    timeOfUseAllowed: null,

    requires: [
        'Mdc.timeofuse.view.PreviewForm',
        'Mdc.timeofuse.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.view,
                itemId: 'touPreviewMenuButton',
                hidden: !me.timeOfUseAllowed,
                menu: {
                    xtype: 'tou-devicetype-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'devicetype-tou-preview-form',
            itemId: 'devicetype-tou-preview-form'
        };
        me.callParent(arguments);
    }

});
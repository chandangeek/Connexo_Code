/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.CommandPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.command-preview',
    frame: true,

    requires: [
        'Mdc.commands.view.CommandPreviewForm',
        'Mdc.commands.view.CommandActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'mdc-command-preview-actions-button',
                hidden: Mdc.privileges.DeviceCommands.executeCommands,
                hidden: true,
                menu: {
                    xtype: 'command-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'command-preview-form',
            itemId: 'mdc-command-preview-form'
        };
        me.callParent(arguments);
    }
});
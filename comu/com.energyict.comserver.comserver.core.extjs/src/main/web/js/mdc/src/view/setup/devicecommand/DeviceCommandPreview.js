/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommand.DeviceCommandPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceCommandPreview',
    requires: [
        'Mdc.view.setup.devicecommand.DeviceCommandPreviewForm',
        'Uni.property.form.Property',
        'Mdc.view.setup.devicecommand.widget.ActionMenu'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    frame: true,
    header: {
        height: 37
    },
    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'commandsPreviewActionButton',
            hidden: Mdc.privileges.DeviceCommands.executeCommands,
            hidden: true,
            menu: {
                xtype: 'device-command-action-menu'
            }
        }
    ],
    items: [
        {
            xtype: 'deviceCommandPreviewForm'
        },
        {
            xtype: 'displayfield',
            margins: '7 0 10 0',
            itemId: 'previewPropertiesHeader'
        },
        {
            xtype: 'panel',
            ui: 'medium',
            itemId: 'previewPropertiesPanel',
            items: [
                {
                    xtype: 'property-form',
                    isEdit: false,
                    defaults: {
                        labelWidth: 200,
                        columnWidth: 0.5
                    }
                }
            ]
        }
    ]
});



/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicegroup.DeviceGroupPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'deviceGroupPreview',
    alias: 'widget.deviceGroup-preview-form',
    frame: true,
    requires: [
        'Mdc.view.setup.devicegroup.DeviceGroupActionMenu',
        'Mdc.view.setup.devicegroup.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'device-group-action-menu'
            }
        }
    ],

    items: {
        xtype: 'devicegroups-preview-form',
        itemId: 'deviceGroupPreviewForm'
    }
});

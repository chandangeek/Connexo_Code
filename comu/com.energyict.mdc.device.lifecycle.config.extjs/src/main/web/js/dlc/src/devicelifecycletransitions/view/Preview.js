/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycletransitions.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycle-transitions-preview',

    requires: [
        'Dlc.devicelifecycletransitions.view.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Dlc.privileges.DeviceLifeCycle.configure,
            menu: {
                xtype: 'transitions-action-menu',
                itemId: 'transitions-action-menu'
            }
        }
    ],

    items: {
        xtype: 'device-life-cycle-transitions-preview-form',
        itemId: 'device-life-cycle-transitions-preview-form'
    }
});

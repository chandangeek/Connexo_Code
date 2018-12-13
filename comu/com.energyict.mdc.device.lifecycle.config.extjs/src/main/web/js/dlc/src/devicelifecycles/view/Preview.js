/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dlc.devicelifecycles.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.device-life-cycles-preview',

    requires: [
        'Dlc.devicelifecycles.view.PreviewForm',
        'Dlc.devicelifecycles.view.ActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Dlc.privileges.DeviceLifeCycle.configure,
            menu: {
                xtype: 'device-life-cycles-action-menu',
                itemId: 'lifeCyclesActionMenu'
            }
        }
    ],

    items: {
        xtype: 'device-life-cycles-preview-form',
        itemId: 'device-life-cycles-preview-form',
        isOverview: false
    }
});

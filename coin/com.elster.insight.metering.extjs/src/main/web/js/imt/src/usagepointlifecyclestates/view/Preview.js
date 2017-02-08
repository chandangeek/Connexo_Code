/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usagepoint-life-cycle-states-preview',

    requires: [
        'Imt.usagepointlifecyclestates.view.PreviewForm',
        'Imt.usagepointlifecyclestates.view.ActionMenu'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Imt.privileges.UsagePointLifeCycle.configure,
            menu: {
                xtype: 'usagepoint-life-cycle-states-action-menu',
                itemId: 'statesActionMenu'
            }
        }
    ],

    items: {
        xtype: 'usagepoint-life-cycle-states-preview-form',
        itemId: 'usagepoint-life-cycle-states-preview-form'
    }
});

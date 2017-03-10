/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroups.view.UsagePointGroupPreview', {
    extend: 'Ext.form.Panel',
    xtype: 'usagepointgroup-preview',
    alias: 'widget.usagepointgroup-preview',
    frame: true,
    requires: [
        'Imt.usagepointgroups.view.UsagePointGroupActionMenu',
        'Imt.usagepointgroups.view.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'usagepointgroup-preview-actions-btn',
            privileges: Imt.privileges.UsagePointGroup.administrateAnyOrStaticGroup,
            menu: {
                xtype: 'usagepointgroup-action-menu',
                itemId: 'usagepointgroup-preview-action-menu'
            }
        }
    ],

    items: {
        xtype: 'usagepointgroup-preview-form',
        itemId: 'usagepointgroup-preview-form'
    }
});

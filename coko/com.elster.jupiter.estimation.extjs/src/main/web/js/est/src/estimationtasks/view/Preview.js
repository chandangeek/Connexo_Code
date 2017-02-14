/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.estimationtasks-preview',
    title: ' ',

    requires: [
        'Est.estimationtasks.view.DetailForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'estimationtasks-action-menu'
            }
        }
    ],

    items: {
        xtype: 'estimationtasks-detail-form',
        itemId: 'estimationtasks-detail-form'
    }
});

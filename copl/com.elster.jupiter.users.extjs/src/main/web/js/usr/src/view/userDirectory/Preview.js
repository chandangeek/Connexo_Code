/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.userDirectory.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.usr-user-directory-preview',
    requires: [
        'Usr.view.userDirectory.PreviewForm'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'btn-user-directory-preview-action-menu',
            menu: {
                xtype: 'usr-user-directory-action-menu'
            }
        }
    ],
    items: {
        xtype: 'usr-user-directory-preview-form',
        itemId: 'pnl-user-directory-preview-form'
    }
});

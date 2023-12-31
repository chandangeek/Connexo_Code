/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Neptune.view.panel.widget.Tool', {
    extend: 'Ext.panel.Panel',
    xtype: 'toolPanel',
    title: 'Tools',
    html: 'Hi, I\'m a panel with all possible tools',
    tools: [
        { type: 'close' },
        { type: 'minimize' },
        { type: 'maximize' },
        { type: 'restore' },
        { type: 'toggle' },
        { type: 'gear' },
        { type: 'prev' },
        { type: 'next' },
        { type: 'pin' },
        { type: 'unpin' },
        { type: 'right' },
        { type: 'left' },
        { type: 'down' },
        { type: 'up' },
        { type: 'refresh' },
        { type: 'plus' },
        { type: 'minus' },
        { type: 'search' },
        { type: 'save' },
        { type: 'help' },
        { type: 'print' },
        { type: 'expand' },
        { type: 'collapse' }
    ]
});
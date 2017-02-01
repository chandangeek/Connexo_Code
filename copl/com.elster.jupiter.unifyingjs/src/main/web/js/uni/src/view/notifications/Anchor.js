/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.notifications.Anchor
 */
Ext.define('Uni.view.notifications.Anchor', {
    extend: 'Ext.button.Button',
    alias: 'widget.notificationsAnchor',

    text: '',
    action: 'preview',
    glyph: 'xe012@icomoon',
    scale: 'small',
    cls: 'notifications-anchor',
    disabled: true,

    menu: [
        {
            xtype: 'dataview',
            tpl: [
                '<tpl for=".">',
                '<div class="notification-item">',
                '<p>{message}</p>',
                '</div>',
                '</tpl>'
            ],
            itemSelector: 'div.notification-item',
            store: 'notifications'
        }
    ]
});
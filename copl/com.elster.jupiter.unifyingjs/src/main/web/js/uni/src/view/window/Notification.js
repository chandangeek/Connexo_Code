/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.window.Notification', {
    extend: 'Ext.window.Window',
    closable: false,
    width: 600,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    floating: true,
    padding: '2px 24px 16px 24px',
    style: {
        borderWidth: '2px',
        borderColor: '#71b668',
        borderRadius: '10px'
    },

    tbar: [
        {
            xtype: 'container',
            itemId : 'window-top-bar-title-container',
            style: {
                color: '#71b668',
                fontFamily: "'Open Sans Condensed', helvetica, arial, verdana, sans-serif",
                fontSize: '24px',
                lineHeight: '36px',
                margin: '0px 0px 0px 16px'
            }
        }
    ],

    bbar: [
        {
            xtype: 'container',
            flex: 1
        },
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.close','UNI','Close'),
            itemId: 'notification-window-close-btn',
            ui: 'action'
        }
    ],

    initComponent: function () {
        var me = this;

        me.callParent(arguments);
        me.down('#notification-window-close-btn').on('click', function() {
            me.destroy();
        });
    },

    setFormTitle: function(title) {
        var titleContainer = this.down('#window-top-bar-title-container');
        titleContainer.update(title);
    }
});
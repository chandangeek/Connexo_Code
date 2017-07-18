/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.notifications.NotificationPanel
 *
 *     @example
 *            {
 *               xtype: 'uni-notification-panel',
 *               title: 'Description',
 *               type: 'confirmation',
 *               message: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.'
 *            },
 *
 *     There three possible styles for this panel: 'error', 'confirmation' and 'success'. This can be changed by 'type' attribute. Default style is 'confirmation'
 *     It is possible to add some custom components to this panel. 'additionalitems' property is responsible for that.
 *
 *     @example with additionalItems
 *            {
 *                xtype: 'uni-notification-panel',
 *                title: 'Description',
 *                type: 'confirmation',
 *                message: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.',
 *                additionalItems: [
 *                   {
 *                      text: Uni.I18n.translate('general.add', 'UNI', 'Add'),
 *                      xtype: 'button',
 *                      ui: 'action'
 *                   },
 *                   {
 *                      text: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),
 *                      xtype: 'button',
 *                      ui: 'link'
 *                    }
 *                ]
 *            },
 *
 */

Ext.define('Uni.view.notifications.NotificationPanel', {
    extend: 'Ext.container.Container',
    xtype: 'uni-notification-panel',
    width: 800,

    type: 'confirmation',
    additionalItems: [],
    layout: {
        type: 'vbox'
    },

    initComponent: function () {
        var me = this,
            message;

        me.items = [
            {
                xtype: 'panel',
                ui: 'medium',
                title: me.title,
                items: [
                    {
                        xtype: 'container',
                        itemId: 'messageContainer',
                        width: 700,
                        margin: '0 0 20 0'
                    },
                    {
                        xtype: 'container',
                        itemId: 'wrapperContainer'
                    }
                ]
            }
        ];

        switch (me.type) {
            case 'error':
                message = '<h3 style="font-weight: 600; color: #EB5642">' + me.message + '</h3>';
                break;
            case 'success':
                message = '<h3 style="font-weight: 600; color: #70bb51">' + me.message + '</h3>';
                break;
            default:
                message = '<h3 style="font-weight: 600">' + me.message + '</h3>';
                break;
        }

        me.callParent(arguments);

        Ext.suspendLayouts();

        var messageContainer = me.down('#messageContainer'),
            wrapperContainer = me.down('#wrapperContainer');

        messageContainer.update(message);

        if (!Ext.isEmpty(me.additionalItems)) {
            wrapperContainer.add(me.additionalItems)
        }

        Ext.resumeLayouts();
    }
});
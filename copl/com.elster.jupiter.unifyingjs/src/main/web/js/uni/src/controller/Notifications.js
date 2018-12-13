/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.controller.Notifications
 */
Ext.define('Uni.controller.Notifications', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.notifications.Anchor',
        'Uni.store.Notifications'
    ],

    refs: [
        {
            ref: 'anchor',
            selector: 'notificationsAnchor'
        }
    ],

    init: function () {
        this.getApplication().on('addnotificationevent', this.addNotification, this);

        Uni.store.Notifications.on({
            add: this.resetAnchorCount,
            load: this.resetAnchorCount,
            update: this.resetAnchorCount,
            remove: this.resetAnchorCount,
            bulk: this.resetAnchorCount,
            scope: this
        });

        this.control({
            'notificationsAnchor': {
                afterrender: this.resetAnchorCount
            }
        });
    },

    addNotification: function (notification) {
        Uni.store.Notifications.add(notification);
    },

    resetAnchorCount: function () {
        var unseenCount = 0;

        Uni.store.Notifications.each(function (record) {
            if (!record.data.timeseen) {
                unseenCount++;
            }
        });

        if (unseenCount > 0) {
            this.getAnchor().enable();
        } else {
            this.getAnchor().disable();
        }

        this.getAnchor().setText(this.getUnseenText(unseenCount));
        // TODO Slightly animate it when the count increases.
    },

    getUnseenText: function (count) {
        var unseenText = '';

        if (count > 10) {
            unseenText = '10+';
        } else if (count > 0) {
            unseenText = count;
        }

        return unseenText;
    }
});
Ext.define('Uni.controller.Notifications', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.notifications.Anchor'
    ],

    refs: [
        {
            ref: 'anchor',
            selector: 'notificationsAnchor'
        }
    ],

    init: function () {
        this.getApplication().on('addnotificationevent', this.addNotification, this);
        Uni.store.Notifications.on('add', this.resetAnchorCount, this);
        Uni.store.Notifications.on('update', this.resetAnchorCount, this);
        Uni.store.Notifications.on('remove', this.resetAnchorCount, this);
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
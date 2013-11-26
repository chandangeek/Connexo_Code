Ext.define('Uni.controller.Notifications', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.view.notifications.Counter'
    ],

    refs: [
        {
            ref: 'counter',
            selector: 'notificationsCounter'
        }
    ],

    init: function () {
        console.log('Initialising Notifications...');
    }
});
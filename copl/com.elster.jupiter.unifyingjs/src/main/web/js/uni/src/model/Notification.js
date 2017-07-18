/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.Notification
 */
Ext.define('Uni.model.Notification', {
    extend: 'Ext.data.Model',

    fields: [
        'message',
        'type',
        'timeadded',
        'timeseen',
        'callback'
    ],

    constructor: function () {
        var data = arguments[0] || {};

        if (!data['timeadded']) {
            data['timeadded'] = new Date();
        }

        if (arguments.length === 0) {
            this.callParent([data]);
        } else {
            this.callParent(arguments);
        }
    },

    proxy: {
        type: 'memory'
    }
});
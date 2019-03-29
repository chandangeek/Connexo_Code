/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Idl.model.Issue', {
    extend: 'Isu.model.Issue',

    fields: [
        {name: 'device_location', persist: false, mapping: 'device.location'},
        {
            name: 'failedTransitionData',
            persist: false,
            mapping: function (data) {

            }

        }
        ],

    proxy: {
        type: 'rest',
        url: '/api/idl/issues',
        reader: {
            type: 'json'
        }
    }
});
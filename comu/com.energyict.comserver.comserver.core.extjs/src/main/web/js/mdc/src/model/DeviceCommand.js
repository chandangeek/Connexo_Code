/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.DeviceCommand', {
    extend: 'Mdc.model.DeviceMessageSpec',
    idProperty: 'id',
    fields: [
        {name: 'trackingIdAndName', type: 'auto', persist:false},
        {name: 'trackingCategory', useNull: true},
        {name: 'category', type: 'string', useNull: true},
        {name: 'status', type: 'auto', useNull: true},
        {name: 'sentDate', type: 'int', useNull: true},
        {name: 'creationDate', type: 'int', useNull: true},
        {name: 'releaseDate', type: 'int', useNull: true},
        {name: 'user', type: 'string', useNull: true},
        {name: 'errorMessage', type: 'string', useNull: true},
        {name: 'messageSpecification', type: 'auto', useNull: true},
        {name: 'userCanAdministrate', type: 'boolean', useNull: true, persist: false},
        {
            name: 'preferredComTask',
            persist: false
        },
        {
            name: 'command',
            persist: false,
            mapping: function (data) {
                var res = {};
                res.name = data.messageSpecification ? data.messageSpecification.name : '-';
                res.willBePickedUpByComTask = data.willBePickedUpByComTask;
                res.willBePickedUpByPlannedComTask = data.willBePickedUpByPlannedComTask;
                res.status = data.status ? data.status.value : null;
                return res
            }
        }
    ],
    proxy: {
        type: 'rest',
        pageParam: false,
        timeout: 60000,
        reader: {
            type: 'json',
            root: 'deviceMessages',
            totalProperty: 'total'
        }
    }
});
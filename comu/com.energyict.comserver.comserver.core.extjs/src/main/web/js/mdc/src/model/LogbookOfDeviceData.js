/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.LogbookOfDeviceData', {
    extend: 'Ext.data.Model',
    idgen: 'sequential',
    fields: [
        {name: 'eventDate', dateFormat: 'time', type: 'date'},
        'eventType',
        'deviceCode',
        'eventLogId',
        {name: 'readingDate', dateFormat: 'time', type: 'date'},
        'logBookId',
        'message',
        {
            name: 'code',
            persist: false,
            mapping: function (data) {
                return data.eventType? data.eventType.code : '';
            }
        },
        {
            name: 'deviceType',
            persist: false,
            mapping: function (data) {
                return data.eventType ? data.eventType.deviceType : '';
            }
        },
        {
            name: 'domain',
            persist: false,
            mapping: function (data) {
                return data.eventType ? data.eventType.domain : '';
            }
        },
        {
            name: 'subDomain',
            persist: false,
            mapping: function (data) {
                return data.eventType ? data.eventType.subDomain : '';
            }
        },
        {
            name: 'eventOrAction',
            persist: false,
            mapping: function (data) {
                return data.eventType ? data.eventType.eventOrAction : '';
            }
        }
    ]
});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.model.ServedMessageService', {
    extend: 'Ext.data.Model',
    fields: [
        'active',
        'subscriberSpec',
        {name: 'numberOfThreads', type: 'int'},
        {
            name: 'messageService',
            persist: false,
            mapping: function (data) {
                return data.subscriberSpec.displayName;
            }
        }
    ]
});
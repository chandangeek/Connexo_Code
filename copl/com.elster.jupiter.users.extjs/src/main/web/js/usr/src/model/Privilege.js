/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.Privilege', {
    extend: 'Ext.data.Model',
    fields: [
        'name',
        'applicationName',
        'translatedName',
        'translatedApplicationName',
        'canGrant',
        {
            name: 'selected',
            type: 'boolean',
            defaultValue: false
        },
        {
            name: 'id', type: 'string', convert: function (value, record) {
            return record.get('name') +'.'+ record.get('applicationName')
        }
        }
    ],
    idProperty: 'id'
});
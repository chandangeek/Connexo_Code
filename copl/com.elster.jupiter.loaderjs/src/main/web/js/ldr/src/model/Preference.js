/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Ldr.model.Preference
 */
Ext.define('Ldr.model.Preference', {
    extend: 'Ext.data.Model',
    fields: [
        'key',
        'value'
    ],
    idProperty: 'key'
});
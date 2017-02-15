/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.Domain', {
    extend: 'Ext.data.Model',
    fields: [
        'domain',
        'localizedValue'
    ],
    idProperty: 'domain'
});
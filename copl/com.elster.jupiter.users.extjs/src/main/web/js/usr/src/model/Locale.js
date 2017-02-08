/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.Locale', {
    extend: 'Ext.data.Model',
    fields: [
        'languageTag',
        'displayValue'
    ],
    idProperty: 'languageTag'
});
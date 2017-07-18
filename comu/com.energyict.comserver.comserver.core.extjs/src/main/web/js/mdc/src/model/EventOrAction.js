/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.EventOrAction', {
    extend: 'Ext.data.Model',
    fields: [
        'eventOrAction',
        'localizedValue'
    ],
    idProperty: 'eventOrAction'
});
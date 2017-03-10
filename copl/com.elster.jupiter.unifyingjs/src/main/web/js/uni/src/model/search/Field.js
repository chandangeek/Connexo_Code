/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.search.Field
 */
Ext.define('Uni.model.search.Field', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'propertyName', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'displayValue', type: 'string'}
    ]
});
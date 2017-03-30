/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.search.PropertyValue
 */
Ext.define('Uni.model.search.PropertyValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'displayValue', type: 'string'},
        {name: 'id', type: 'string'}
    ]
});
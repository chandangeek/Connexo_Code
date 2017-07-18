/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.model.PropertyValidationRule', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'allowDecimals'},
        {name: 'minimumValue'},
        {name: 'maximumValue'}
    ]
});
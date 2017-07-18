/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.model.PropertyValue', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'value'},
        {name: 'defaultValue'},
        {name: 'inheritedValue'},
        {name: 'propertyHasValue', type:'boolean', persist: false}
    ]
});
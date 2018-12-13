/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.model.SearchCriteria', {
    extend: 'Ext.data.Model',
    fields: [
        {name:'criteriaName', type: 'string'},
        {name: 'criteriaValues', type: 'auto'}
    ]
});

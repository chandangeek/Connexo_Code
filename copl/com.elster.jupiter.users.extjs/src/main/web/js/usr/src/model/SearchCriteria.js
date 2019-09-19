/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.model.SearchCriteria', {

    extend: 'Ext.data.Model',

    fields: [
        {name: 'criteriaName', type: 'string'},
        {name: 'criteriaValues', type: 'auto'}
    ]

});
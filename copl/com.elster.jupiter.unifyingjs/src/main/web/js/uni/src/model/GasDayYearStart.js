/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.model.GasDayYearStart', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'month', type: 'int'},
        {name: 'day', type: 'int'},
        {name: 'hours', type: 'int'},
        {name: 'minutes', type: 'int'},
        {name: 'seconds', type: 'int'}
    ]
});
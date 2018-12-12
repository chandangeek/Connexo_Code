/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.timeofuse.DaysPerType
 */
Ext.define('Uni.model.timeofuse.DaysPerType', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'dayTypeId', type: 'number'},
        {name: 'days', type: 'auto'}
    ]
});
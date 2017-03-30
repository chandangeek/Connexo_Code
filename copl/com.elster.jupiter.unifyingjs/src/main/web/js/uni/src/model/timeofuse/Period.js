/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.timeofuse.Period
 */
Ext.define('Uni.model.timeofuse.Period', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'name', type: 'string'},
        {name: 'fromMonth', type: 'number'},
        {name: 'fromDay', type: 'number'}
    ]
});
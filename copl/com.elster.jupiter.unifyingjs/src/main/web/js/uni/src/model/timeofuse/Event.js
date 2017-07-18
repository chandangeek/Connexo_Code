/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.model.timeofuse.Event
 */
Ext.define('Uni.model.timeofuse.Event', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'id', type: 'number'},
        {name: 'name', type: 'string'},
        {name: 'code', type: 'number'}
    ]
});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tme.model.RelativePeriodUsage', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'task', type: 'string'},
        {name: 'type', type: 'string'},
        {name: 'application'},
        {name: 'nextRun'}
    ],


    proxy: {
        type: 'rest',
        url: '/api/tmr/relativeperiods',
        reader: {
            type: 'json'
        }
    }
});

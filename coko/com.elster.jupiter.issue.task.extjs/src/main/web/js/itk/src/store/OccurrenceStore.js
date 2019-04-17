/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.store.OccurrenceStore', {
    extend: 'Ext.data.Store',

    fields: [
        {name: 'triggerTime', type: 'auto'},
        {name: 'startDate', type: 'auto'},
        {name: 'enddate', type: 'auto'},
        {name: 'status', type: 'auto'},
        {name: 'errorMessage', type: 'auto'},
        {name: 'failureTime', type: 'auto'},
        {
            name: 'duration', mapping: function (data) {
                if (data.enddate && data.startDate) {
                    return (data.enddate - data.startDate)/100;
                }
                return '-';
            }
        }
    ]
});
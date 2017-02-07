/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuseondevice.model.NextPassiveCalendar', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'name', type: 'string'},
        {name: 'releaseDate', type: 'number'},
        {name: 'activationDate', type: 'number'},
        {name: 'status', type: 'string'},
        {name: 'willBePickedUpByPlannedComtask', type: 'boolean'},
        {name: 'willBePickedUpByComtask', type: 'boolean'},
        {
            name: 'releaseDateDisplayField',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                var releaseDate = record.get('releaseDate');
                if (releaseDate && (releaseDate !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(releaseDate));
                }
                return '-';
            }
        },
        {
            name: 'activationDateDisplayField',
            type: 'string',
            persist: false,
            convert: function (value, record) {
                var activationDate = record.get('activationDate');
                if (activationDate && (activationDate !== 0)) {
                    return Uni.DateTime.formatDateTimeLong(new Date(activationDate));
                }
                return '-';
            }
        }
    ]
});

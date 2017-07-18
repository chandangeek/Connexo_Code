/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.AvailableCalendarsGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.tou-available-cal-grd',
    requires: [
        'Mdc.timeofuse.store.UnusedCalendars'
    ],
    store: 'Mdc.timeofuse.store.UnusedCalendars',
    deviceTYpeId: null,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('timeofuse.nrOfMessageServices.selected', count, 'MDC',
            'No time of use calendars selected', '{0} time of use calendar selected', '{0} time of use calendars selected'
        );
    },

    bottomToolbarHidden: true,
    checkAllButtonPresent: true,


    columns: [
        {
            header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            dataIndex: 'name',
            flex: 1
        }
    ]
});



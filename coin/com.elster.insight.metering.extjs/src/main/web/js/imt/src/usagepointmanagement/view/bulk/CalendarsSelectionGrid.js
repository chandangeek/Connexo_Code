/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.bulk.CalendarsSelectionGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.calendars-selection-grid',
    itemId: 'calendarsgrid',
    store: 'Imt.usagepointmanagement.store.ActiveCalendars',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.calendars.selected', count, 'IMT',
            'No calendars selected', '{0} calendar selected', '{0} calendars selected'
        );
    },
    selectedDescription: Uni.I18n.translate('usagepoints.bulk.selectedScheduleInTable', 'IMT', 'Select calendars in table'),

    cancelHref: '#/search',

    margin: '0 0 -20 0',

    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.calendar', 'IMT', 'Calendar'),
                dataIndex: 'name',
                flex: 1
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        //     this.down('#bottomToolbar').setVisible(false);
    }
});
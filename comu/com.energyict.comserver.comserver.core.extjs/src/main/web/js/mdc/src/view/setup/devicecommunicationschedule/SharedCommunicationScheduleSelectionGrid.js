/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationschedule.SharedCommunicationScheduleSelectionGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.sharedCommunicationScheduleSelectionGrid',
    overflowY: 'auto',
    itemId: 'sharedCommunicationScheduleSelectionGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    columns: [
        {
            header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            dataIndex: 'name',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 1
        },
        {
            header: Uni.I18n.translate('deviceCommunicationSchedule.frequency', 'MDC', 'Frequency'),
            dataIndex: 'temporalExpression',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 1,
            renderer: function(value){
                return Mdc.util.ScheduleToStringConverter.convert(value);
            }
        },
        {
            header: Uni.I18n.translate('deviceCommunicationSchedule.plannedDate', 'MDC', 'Planned date'),
            dataIndex: 'plannedDate',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 1,
            renderer: function (value) {
                return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeLong(new Date(value));
            }
        }
    ],

    initComponent: function(){
        this.counterTextFn = function (count) {
            return Uni.I18n.translatePlural(
                'general.nrOfSharedComSchedules.selected', count, 'MDC',
                'No shared communication schedules selected',
                '{0} shared communication schedule selected',
                '{0} shared communication schedules selected'
            );
        };
        this.callParent(arguments);
    }


});


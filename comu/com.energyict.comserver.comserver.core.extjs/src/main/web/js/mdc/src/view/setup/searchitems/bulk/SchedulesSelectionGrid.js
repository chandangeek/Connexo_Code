Ext.define('Mdc.view.setup.searchitems.bulk.SchedulesSelectionGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.schedules-selection-grid',
    itemId: 'schedulesgrid',
    store: 'Mdc.store.CommunicationSchedulesWithoutPaging',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfSharedComSchedules.selected', count, 'MDC',
            'No shared communication schedules selected', '{0} shared communication schedule selected', '{0} shared communication schedules selected'
        );
    },
    selectedDescription: Uni.I18n.translate('searchItems.bulk.selectedScheduleInTable', 'MDC', 'Select shared communication schedules in table'),

    cancelHref: '#/search',

    margin: '0 0 -20 0',

    columns: {
        items: [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('communicationschedule.schedule', 'MDC', 'Schedule'),
                dataIndex: 'temporalExpression',
                renderer: function (value) {
                    return Mdc.util.ScheduleToStringConverter.convert(value);
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('communicationschedule.plannedDate', 'MDC', 'Planned date'),
                dataIndex: 'plannedDate',
                renderer: function (value) {
                    return Uni.I18n.formatDate('general.dateFormat.long', value, 'MDC', 'M d Y H:i A');
                },
                flex: 1
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
   //     this.down('#bottomToolbar').setVisible(false);
    }
});
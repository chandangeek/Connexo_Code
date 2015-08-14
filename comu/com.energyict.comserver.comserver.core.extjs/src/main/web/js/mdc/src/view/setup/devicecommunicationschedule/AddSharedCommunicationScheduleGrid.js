Ext.define('Mdc.view.setup.devicecommunicationschedule.AddSharedCommunicationScheduleGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.addSharedCommunicationScheduleGrid',
    overflowY: 'auto',
    itemId: 'addSharedCommunicationScheduleGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.AvailableCommunicationSchedulesForDevice'
    ],
    store: 'AvailableCommunicationSchedulesForDevice',
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
                if (value !== null) {
                    return Uni.DateTime.formatDateTimeLong(new Date(value));
                } else {
                    return '';
                }
            }
        }
    ],

    initComponent: function(){
        this.counterTextFn = function (count) {
            return Uni.I18n.translatePlural(
                'deviceCommunicationSchedule.BulkSelection.counterText',
                count,
                'MDC',
                '{0} shared communication schedules selected'
            );
        };
        this.callParent(arguments);
    }


});


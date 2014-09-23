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
    height: 300,
    store: 'AvailableCommunicationSchedulesForDevice',
    columns: [
        {
            header: Uni.I18n.translate('deviceCommunicationSchedule.name', 'MDC', 'Name'),
            dataIndex: 'name',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 0.9
        },
        {
            header: Uni.I18n.translate('deviceCommunicationSchedule.frequency', 'MDC', 'Frequency'),
            dataIndex: 'temporalExpression',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 0.9,
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
            flex: 0.9,
            renderer: function (value) {
                if (value !== null) {
                    return new Date(value).toLocaleString();
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
                'UNI',
                '{0} communication schedules selected'
            );
        };
        this.callParent(arguments);
    }


});


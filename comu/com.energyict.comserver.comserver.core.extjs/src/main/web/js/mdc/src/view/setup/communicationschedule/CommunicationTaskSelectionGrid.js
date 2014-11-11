Ext.define('Mdc.view.setup.communicationschedule.CommunicationTaskSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.communicationTaskSelectionGrid',
    overflowY: 'auto',
    maxHeight: 300,
    itemId: 'communicationTaskGridFromSchedule',
    store: 'CommunicationTasksForCommunicationSchedule',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationSchedules',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu'
    ],
    radioHidden: true,
    bottomToolbarHidden: true,

    allLabel: Uni.I18n.translate('communicationschedule.communicationTasks.allLabel', 'MDC', 'All communication tasks'),
    allDescription: Uni.I18n.translate(
        'communicationschedule.communicationTasks.allDescription',
        'MDC',
        'Select all communication tasks'
    ),

    selectedLabel: Uni.I18n.translate('communicationschedule.communicationTasks.selectedLabel', 'MDC', 'Selected communication tasks'),
    selectedDescription: Uni.I18n.translate(
        'communicationschedule.communicationTasks.selectedDescription',
        'MDC',
        'Select communication tasks in table'
    ),

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'communicationtask.communicationTask',
            count,
            'MDC',
            '{0} communication tasks selected'
        );
    },
    columns: [
        {
            header: Uni.I18n.translate('communicationtask.name', 'MDC', 'Name'),
            dataIndex: 'name',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 0.9
        }
    ],

    initComponent: function () {
        var me = this;
        me.callParent(arguments);
    }
});

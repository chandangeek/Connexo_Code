Ext.define('Mdc.view.setup.communicationschedule.CommunicationTaskSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.communicationTaskSelectionGrid',
    itemId: 'communicationTaskGridFromSchedule',
    store: 'CommunicationTasksForCommunicationSchedule',
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
    extraTopToolbarComponent: [
        '->',
        {
            xtype: 'button',
            action: 'cancelAction',
            ui: 'link',
            text: Uni.I18n.translate('communicationschedule.manageCommunicationTasks', 'MDC', 'Manage communication tasks'),
            href: '#/administration/communicationtasks'
        }
    ],
    columns: [
        {
            header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
            dataIndex: 'name',
            sortable: false,
            hideable: false,
            fixed: true,
            flex: 1
        }
    ],
    onChangeSelectionGroupType: function (radiogroup, value) {
        var me = this;
        if (me.view) {
            var selection = me.view.getSelectionModel().getSelection();

            me.up('#addCommunicationTaskWindow').down('#addCommunicationTasksToSchedule').setDisabled(!me.isAllSelected() && selection.length === 0);
            me.setGridVisible(!me.isAllSelected());
        }
    },
    onSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.up('#addCommunicationTaskWindow').down('#addCommunicationTasksToSchedule').setDisabled(!me.isAllSelected() && selection.length === 0);
        me.getUncheckAllButton().setDisabled(selection.length === 0);
    }
});

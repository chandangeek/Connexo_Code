Ext.define('Mdc.view.setup.communicationschedule.CommunicationTaskSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.communicationTaskSelectionGrid',
    overflowY: 'auto',
    itemId: 'communicationTaskSelectionGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationSchedules',
        'Mdc.view.setup.communicationschedule.CommunicationScheduleActionMenu'
    ],
    radioHidden: true,
    bottomToolbarHidden: true,
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
        me.getSelectionGroupType().on('change', me.onChangeSelection, me);
    },

    onChangeSelection: function () {
        var me = this,
            selectionModel = me.view.getSelectionModel(),
            selection = selectionModel.getSelection(),
            comTaskWindow = me.up('#addCommunicationTaskWindow'),
            preview = comTaskWindow.down('addCommunicationTaskPreview');

        if (!me.isAllSelected() && selection.length != 0) {
            preview.show();
        } else {
            preview.hide();
        }
    }

});

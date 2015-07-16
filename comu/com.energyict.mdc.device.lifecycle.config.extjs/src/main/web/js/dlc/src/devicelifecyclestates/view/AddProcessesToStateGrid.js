Ext.define('Dlc.devicelifecyclestates.view.AddProcessesToStateGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.AddProcessesToStateGrid',
    xtype: 'add-process-to-state-selection-grid',
    requires: [
        'Dlc.devicelifecyclestates.store.AvailableTransitionBusinessProcesses'
    ],
    plugins: {
        ptype: 'bufferedrenderer'
    },
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'transitionBusinessProcesses.numberSelected',
            count,
            'DLC',
            '{0} processes selected'
        );
    },
    bottomToolbarHidden: true,
    columns: {
        items: [
            {
                header: Uni.I18n.translate('transitionBusinessProcesses.process', 'DLC', 'Process'),
                dataIndex: 'processId',
                flex: 1
            }
        ]
    }
});
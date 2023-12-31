/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecyclestates.view.AddProcessesToStateGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-process-to-state-selection-grid',
    xtype: 'add-process-to-state-selection-grid',
    requires: [
        'Imt.usagepointlifecyclestates.store.AvailableTransitionBusinessProcesses'
    ],
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfProcesses.selected', count, 'IMT',
            'No processes selected', '{0} process selected', '{0} processes selected'
        );
    },
    checkAllButtonPresent: true,
    columns: {
        items: [
            {
                header: Uni.I18n.translate('transitionBusinessProcesses.process.name', 'IMT', 'Name'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    return value + ' (' + record.get('version') + ')';
                },
                flex: 1
            }
        ]
    }
});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.AddDeviceStates', {
    extend: 'Uni.view.grid.SelectionGrid',
    xtype: 'dbp-add-device-states-grid',
    store: 'Dbp.processes.store.DeviceStates',
    height: 310,
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'editProcess.nrOfDeviceStates.selected', count, 'DBP',
            'No device states selected', '{0} device state selected', '{0} device states selected'
        );
    },

    columns: {
        items: [
            {
                header: Uni.I18n.translate('editProcess.deviceLifeCycle', 'DBP', 'Device life cycle'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('editProcess.deviceState', 'DBP', 'Device state'),
                dataIndex: 'deviceState',
                flex: 1
            }
        ]
    },
    buttonAlign: 'left',
    buttons: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('editProcess.add', 'DBP', 'Add'),
            itemId: 'btn-add-deviceStates',
            action: 'addSelectedDeviceStates',
            disabled: true,
            ui: 'action'
        },
        {
            xtype: 'button',
            itemId: 'btn-cancel-add-deviceStates',
            text: Uni.I18n.translate('editProcess.cancel', 'DBP', 'Cancel'),
            href: '#',
            ui: 'link'
        }
    ]

});


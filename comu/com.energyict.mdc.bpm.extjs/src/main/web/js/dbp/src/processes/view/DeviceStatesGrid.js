Ext.define('Dbp.processes.view.DeviceStatesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-states-grid',
    overflowY: 'auto',
    selModel: {
        mode: 'SINGLE'
    },
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dbp.processes.view.DeviceStatesActionMenu'
    ],
    store: 'ext-empty-store',


    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('editProcess.deviceLifeCycle', 'DBP', 'Device life cycle'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('editProcess.deviceState', 'DBP', 'Device state'),
                dataIndex: 'deviceState',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                menu: {
                    xtype: 'dbp-device-states-action-menu',
                    itemId: 'mnu-device-states-action'
                }
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                usesExactCount: true,
                dock: 'top',
                displayMsg: Uni.I18n.translatePlural('deviceState.pagingtoolbartop.displayMsg', 0, 'DBP', 'No device state', '{0} deivice state', '{0} device states'),
                emptyMsg: Uni.I18n.translate('deviceState.pagingtoolbartop.emptyMsg', 'DBP', 'There are no device states'),
                items: [
                    {
                        text: Uni.I18n.translate('deviceState.addDeviceStates', 'DBP', 'Add device states'),
                        itemId: 'addDeviceStates',
                        privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                        xtype: 'button',
                        action: 'addDeviceState',
                        href: ''
                    }
                ]
            }
        ];

        this.callParent();
    }
})
;

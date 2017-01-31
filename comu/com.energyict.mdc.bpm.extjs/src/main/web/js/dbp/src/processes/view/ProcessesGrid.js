/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.ProcessesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.dbp-processes-grid',
    store: 'Dbp.processes.store.Processes',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dbp.processes.store.Processes'
    ],
    viewConfig: {
        markDirty: false
    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('dbp.process.name', 'DBP', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('dbp.process.version', 'DBP', 'Version'),
                dataIndex: 'version',
                flex: 1
            },
            {
                header: Uni.I18n.translate('dbp.process.associated', 'DBP', 'Associated to'),
                dataIndex: 'associatedTo',
                flex: 1
            },
            {
                header: Uni.I18n.translate('dbp.process.status', 'DBP', 'Status'),
                dataIndex: 'active',
                flex: 2,
                renderer: function (value, metaData, record) {
                    switch (value) {
                        case 'ACTIVE':
                            return Uni.I18n.translate('dbp.process.active', 'DBP', 'Active');
                            break;
                        case 'INACTIVE':
                            return Uni.I18n.translate('dbp.process.inactive', 'DBP', 'Inactive');
                            break;
                        case 'UNDEPLOYED':
                            return Uni.I18n.translate('dbp.process.undeployed', 'DBP', 'Undeployed');
                            break;
                        default:
                            return value;
                    }
                }

            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                width: 100,
                menu: {
                    xtype: 'dbp-process-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('dbp.process.pagingtoolbartop.displayMsg', 'DBP', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('dbp.process.pagingtoolbartop.displayMoreMsg', 'DBP', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('dbp.process.pagingtoolbartop.emptyMsg', 'DBP', 'There are no process to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('dbp.process.pagingtoolbarbottom.itemsPerPage', 'DBP', 'Processes per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

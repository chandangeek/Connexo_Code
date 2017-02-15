/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.ProcessesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-processes-grid',
    store: 'Bpm.processes.store.Processes',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Bpm.processes.store.Processes'
    ],
    viewConfig: {
        markDirty: false
    },
    disableAction: false,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('bpm.process.name', 'BPM', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('bpm.process.version', 'BPM', 'Version'),
                dataIndex: 'version',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.process.associated', 'BPM', 'Associated to'),
                dataIndex: 'displayType',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.process.status', 'BPM', 'Status'),
                dataIndex: 'active',
                flex: 2,
                renderer: function (value, metaData, record) {
                    switch (value) {
                        case 'ACTIVE':
                            return Uni.I18n.translate('bpm.process.active', 'BPM', 'Active');
                            break;
                        case 'INACTIVE':
                            return Uni.I18n.translate('bpm.process.inactive', 'BPM', 'Inactive');
                            break;
                        case 'UNDEPLOYED':
                            return Uni.I18n.translate('bpm.process.undeployed', 'BPM', 'Undeployed');
                            break;
                        default:
                            return Ext.isEmpty(value) ? '-' : value;
                    }
                }

            },
            {
                header: Uni.I18n.translate('bpm.process.application', 'BPM', 'Application'),
                dataIndex: 'appKey',
                flex: 2,
                renderer: function (value, metaData, record) {
                    switch (value) {
                        case 'INS':
                            return Uni.I18n.translate('bpm.process.insight', 'BPM', 'Insight');
                            break;
                        case 'MDC':
                            return Uni.I18n.translate('bpm.process.mdc', 'BPM', 'MultiSense');
                            break;
                        default:
                            return Ext.isEmpty(value) ? '-' : value;
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                width: 100,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    return me.disableAction;
                },
                menu: {
                    xtype: 'bpm-process-action-menu'
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('bpm.process.pagingtoolbartop.displayMsg', 'BPM', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('bpm.process.pagingtoolbartop.displayMoreMsg', 'BPM', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('bpm.process.pagingtoolbartop.emptyMsg', 'BPM', 'There are no process to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('bpm.process.pagingtoolbarbottom.itemsPerPage', 'BPM', 'Processes per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});

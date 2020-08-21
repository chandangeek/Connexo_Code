/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.processes.view.AllProcessesGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    xtype: 'processesGrid',

    router: null,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.processes.store.AllProcessesStore',
        'Mdc.processes.store.ProcessesBuffered',
        'Mdc.processes.view.AllProcessesTopFilter',
        'Mdc.processes.view.AllProcessesSortingMenu'
    ],


    selModel: {
        mode: 'SINGLE'
    },

    store: 'Mdc.processes.store.AllProcessesStore',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('mdc.process.processId', 'MDC', 'Process ID'),
                dataIndex: 'processId',
                flex: 1
            },
            {
                header: Uni.I18n.translate('mdc.process.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('mdc.process.type', 'MDC', 'Type'),
                dataIndex: 'type',
                flex: 2
            },
            {
                header: Uni.I18n.translate('mdc.process.object', 'MDC', 'Object'),
                dataIndex: 'objectName',
                flex: 2,



                renderer: function (value, b, record) {
                    var result = '';
                    if(record.data.type == 'Device'){
                        if (value && Mdc.privileges.Device.canView()) {
                            var url = me.router.getRoute('devices/device').buildUrl({deviceId: value});
                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                            }else if (value){
                                result = value;
                            }
                        }

                    if(record.data.type == 'Alarm'){
                        if (value && Dal.privileges.Alarm.canViewAdmimAlarm()) {
                            var url = me.router.getRoute('workspace/alarms/view').buildUrl({alarmId: record.get('value')});
                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                        } else if (value){
                            result = value;
                        }
                    }

                    if(record.data.type == 'Issue'){
                        if (value && Isu.privileges.Issue.canViewAdminDevice()) {
                            var url = me.router.getRoute('workspace/issues/view').buildUrl({issueId: record.get('value')}, {issueType: record.get('issueType')});
                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                        } else if (value){
                            result = value;
                        }
                    }

                    return result;
                }
            },
            {
                header: Uni.I18n.translate('mdc.process.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startDateDisplay',
                flex: 2
            },
            {
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                shortFormat: true,
                textAlign: 'center',
                flex: 1
            },
            {
                header: Uni.I18n.translate('mdc.process.status', 'MDC', 'Status'),
                dataIndex: 'statusDisplay',
                flex: 1
            },
            {
                header: Uni.I18n.translate('mdc.process.startedBy', 'MDC', 'Started by'),
                dataIndex: 'startedBy',
                flex: 1
            }
        ];

        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: this.store,
                dock: 'top',
                needCustomExporter: true,
                displayMsg: Uni.I18n.translate('mdc.process.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('mdc.process.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('mdc.process.pagingtoolbartop.emptyMsg', 'MDC', 'There are no process to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'alarms-bulk-action',
                        text: Uni.I18n.translate('mdc.process.bulkActions', 'MDC', 'Bulk action'),
                        privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                        action: 'showBulkActions',
                        handler: function () {
                            me.router.getRoute(me.router.currentRoute + '/bulkaction').forward(me.router.arguments, Uni.util.QueryString.getQueryStringValues(false));
                        }
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('mdc.process.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Processes per page'),
                dock: 'bottom'
            }
        ];
        this.callParent();
    }
});

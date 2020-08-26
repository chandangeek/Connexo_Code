/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.processes.view.InsightProcessesGrid', {
    extend: 'Ext.grid.Panel',
    overflowY: 'auto',
    xtype: 'processesGrid',

    router: null,

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Imt.processes.store.InsightProcessesStore',
		'Imt.processes.view.InsightProcessesTopFilter',
        'Imt.processes.view.InsightProcessesSortingMenu'
    ],


    selModel: {
        mode: 'SINGLE'
    },

    store: 'Imt.processes.store.InsightProcessesStore',

    initComponent: function () {
        var me = this;
        this.columns = [
            {
                header: Uni.I18n.translate('imt.process.processId', 'IMT', 'Process ID'),
                dataIndex: 'processId',
                flex: 1
            },
            {
                header: Uni.I18n.translate('imt.process.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 2
            },
            {
                header: Uni.I18n.translate('imt.process.type', 'IMT', 'Type'),
                dataIndex: 'type',
                flex: 2
            },
            {
                header: Uni.I18n.translate('imt.process.object', 'IMT', 'Object'),
                dataIndex: 'objectName',
                flex: 2,

                renderer: function (value, b, record) {
                    var result = '';
                    if(record.data.type == 'Device'){
                        if (value && Imt.privileges.UsagePoint.canView()) {
                            var url = me.router.getRoute('usagepoints/device').buildUrl({deviceId: value});
                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                        } else if (value) {
                            result = value;
                        }
                    }
                    
                    if(record.data.type == 'UsagePoint'){
                        if (value && Imt.privileges.UsagePoint.canView()) {
                            var url = me.router.getRoute('usagepoints/view').buildUrl({usagePointId: value});
                            result = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                        } else if (value) {
                            result = value;
                        }
                    }
                        
                    if(record.data.type == 'Alarm'){
                        if (value) {
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
                header: Uni.I18n.translate('imt.process.startedOn', 'IMT', 'Started on'),
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
                header: Uni.I18n.translate('imt.process.status', 'IMT', 'Status'),
                dataIndex: 'statusDisplay',
                flex: 1
            },
            {
                header: Uni.I18n.translate('imt.process.startedBy', 'IMT', 'Started by'),
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
                displayMsg: Uni.I18n.translate('imt.process.pagingtoolbartop.displayMsg', 'IMT', '{0} - {1} of {2} processes'),
                displayMoreMsg: Uni.I18n.translate('imt.process.pagingtoolbartop.displayMoreMsg', 'IMT', '{0} - {1} of more than {2} processes'),
                emptyMsg: Uni.I18n.translate('imt.process.pagingtoolbartop.emptyMsg', 'IMT', 'There are no process to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: this.store,
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('imt.process.pagingtoolbarbottom.itemsPerPage', 'IMT', 'Processes per page'),
                dock: 'bottom'
            }
        ];
        this.callParent();
    }
});

Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionHistoryGrid',
    itemId: 'deviceConnectionHistoryGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Duration'
    ],
    store: 'DeviceConnectionHistory',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'startedOn',
                text: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 2,
                usesSeconds: true
            },
            {
                itemId: 'status',
                text: Uni.I18n.translate('deviceconnectionhistory.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function(status,metadata,rowObject){
                    return status!==''?'<a href="#/devices/'+this.mRID+ '/connectionmethods/' + this.connectionId + '/history/' + rowObject.get('id') + '/viewlog?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D' + '">' + status + '</a>':'';
                }
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return value.displayValue;
                    }
                }
            },
            {
                dataIndex: 'comTaskCount',
                itemId: 'comTaskCount',
                renderer: function (val,metaData) {
                    metaData.tdCls = 'communication-tasks-status';
                    var template = '';
                    if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                        template += '<tpl><span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</tpl>';
                        template += '<tpl><span class="icon-close"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</tpl>';
                        template += '<tpl><span  class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</tpl>';
//                        template += '<tpl><img src="/apps/dsh/resources/images/widget/running.png" class="ct-result ct-success" title="Success"><span style="position: relative; top: -3px; left: 4px">' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0') + '</span></tpl>';
//                        template += '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" class="ct-result ct-failure" title="Failed" style="position: relative; left: 30px"><span style="position: relative; top: -3px; left: 34px">' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0') + '</span></tpl>';
//                        template += '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" class="ct-result ct-incomplete" title="Not executed" style="position: relative; left: 56px"><span  style="position: relative; top: -3px; left: 60px">' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0') + '</span></tpl>';
                    }
                    return template;
                },
                header: Uni.I18n.translate('deviceconnectionhistory.communicationTasks', 'DSH', 'Communication tasks'),
                flex: 2
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn'
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} connections'),
                displayMoreMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} connections'),
                emptyMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.emptyMsg', 'MDC', 'There are no connections to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mRID: me.mRID},
                    {connectionId:me.connectionId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connections per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;

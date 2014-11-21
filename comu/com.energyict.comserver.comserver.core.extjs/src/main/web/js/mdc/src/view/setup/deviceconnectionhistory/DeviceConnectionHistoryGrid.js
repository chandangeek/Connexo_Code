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
                renderer: function (value, metadata) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    }
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 1,
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
                renderer: function (val) {
                    var success = val.numberOfSuccessfulTasks ? '<tpl><img src="/apps/dsh/resources/images/widget/running.png" class="ct-result ct-success"><span style="position: relative; top: -3px; left: 4px">' + val.numberOfSuccessfulTasks + '</span></tpl>' : '',
                        failed = val.numberOfFailedTasks ? '<tpl><img src="/apps/dsh/resources/images/widget/blocked.png" class="ct-result ct-failure"><span style="position: relative; top: -3px; left: 4px">' + val.numberOfFailedTasks + '</span></tpl>' : '',
                        notCompleted = val.numberOfIncompleteTasks ? '<tpl><img src="/apps/dsh/resources/images/widget/stopped.png" class="ct-result ct-incomplete"><span  style="position: relative; top: -3px; left: 4px">' + val.numberOfIncompleteTasks + '</span></tpl>' : ''
                        ;
                    return success + failed + notCompleted
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

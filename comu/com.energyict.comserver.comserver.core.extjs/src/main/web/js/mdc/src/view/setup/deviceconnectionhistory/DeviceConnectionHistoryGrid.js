Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceConnectionHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceConnectionHistoryGrid',
    itemId: 'deviceConnectionHistoryGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
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
                flex: 1,
                renderer: function (value,metadata) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    }
                }
            },
            {
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 1,
                renderer: function (value,metadata) {
                    if (value !== null) {
                        return value + ' ' + Uni.I18n.translate('general.seconds', 'MDC', 'seconds');
                    }
                }
            },
            {
                itemId: 'status',
                text: Uni.I18n.translate('deviceconnectionhistory.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1,
                renderer: function(value){
                    if(value){
                        return value.displayValue;
                    }
                }
            },
            {
                dataIndex: 'comTaskCount',
                itemId: 'comTaskCount',
                renderer: function (val) {
                    var success = val.numberOfSuccessfulTasks ? '<tpl><span class="fa fa-check fa-lg" style="color: green; position: relative; vertical-align: 0% !important;"></span><span style="position: relative; left: 4px">' + val.numberOfSuccessfulTasks + '</span></tpl>' : '',
                        failed = val.numberOfFailedTasks ? '<tpl><span class="fa fa-times fa-lg" style="color: red; position: relative; left: 26px; vertical-align: 0% !important;"></span><span style="position: relative; left: 30px">' + val.numberOfFailedTasks + '</span></tpl>' : '',
                        notCompleted = val.numberOfIncompleteTasks ? '<tpl><span class="fa fa-ban fa-lg" style="color: #333333; position: relative; left: 52px; vertical-align: 0% !important"></span><span  style="position: relative; left: 56px">' + val.numberOfIncompleteTasks + '</span></tpl>' : ''
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
    initComponent: function(){
        var me=this;
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
//            params: [
//                {mrid: me.mrid}
//            ],
            itemsPerPageMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Connections per page'),
            dock: 'bottom'
        }
        ];
        me.callParent();
    }

})
;

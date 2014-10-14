Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskExecutionGrid',
    itemId: 'deviceCommunicationTaskExecutionGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    store: 'DeviceCommunicationTaskExecutions',
//    selModel: Ext.create('Ext.selection.Model', { preventFocus: true }),
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('deviceconnectionhistory.communication', 'MDC', 'Communication'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('deviceconnectionhistory.device', 'MDC', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function(value){
                   return value.name;
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('deviceconnectionhistory.currentState', 'MDC', 'Current state'),
                dataIndex: 'currentState',
                flex: 0.5
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 0.5
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startTime',
                flex: 1,
                renderer: function (value,metadata) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    }
                }
            },
            {
                itemId: 'finishTime',
                text: Uni.I18n.translate('deviceconnectionhistory.finishedOn', 'MDC', 'Finished on'),
                dataIndex: 'finishTime',
                flex: 1,
                renderer: function (value,metadata) {
                    if (value !== null) {
                        return new Date(value).toLocaleString();
                    }
                }
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
                displayMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('deviceconnectionhistory.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communications to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
//            params: [
//                {mrid: me.mrid}
//            ],
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('deviceconnectionmethod.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communications per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;

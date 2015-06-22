Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.DeviceCommunicationTaskHistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskHistoryGrid',
    itemId: 'deviceCommunicationTaskHistoryGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Duration'
    ],
    store: 'DeviceCommunicationTaskHistory',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'startedOn',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startTime',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 1,
                usesSeconds: true
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1
            },
            {
                itemId: 'comSession',
                text: Uni.I18n.translate('devicecommunicationtaskhistory.connectionUsed', 'MDC', 'Connection used'),
                dataIndex: 'comSession',
                flex: 1,
                renderer: function(value){
                    if(value){
                        return Ext.String.htmlEncode(value.connectionMethod.name);
                    } else {
                        return '';
                    }
                }
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                menu: {
                    xtype: 'device-communication-task-history-action-menu'
                }
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
                displayMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communications to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                params: [
                    {mRID: me.mRID},
                    {comTaskId:me.comTaskId}
                ],
                itemsPerPageMsg: Uni.I18n.translate('devicecommunicationtaskhistory.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communications per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;

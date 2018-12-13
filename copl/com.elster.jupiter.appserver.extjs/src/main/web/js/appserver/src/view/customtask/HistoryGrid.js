/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.HistoryGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.ctk-tasks-history-grid',
    store: 'Apr.store.CustomTaskHistory',
    router: null,

    requires: [
        'Uni.grid.column.Action',
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Apr.view.customtask.HistoryActionMenu',
        'Uni.DateTime'
    ],
    historyActionItemId: 'historyActionItemId',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('customTask.general.startedOn', 'APR', 'Started on'),
                dataIndex: 'startedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    var url = me.router.getRoute(me.viewLogRoute).buildUrl({occurrenceId: record.get('id')}),
                        date = value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';

                    return me.canHistoryLog ? '<a href="' + url + '">' + date + '</a>' : date;
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                dataIndex: 'duration',
                shortFormat: true,
                textAlign: 'center',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'APR', 'Status'),
                dataIndex: 'status',
                textAlign: 'center',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: me.canHistoryLog,
                menu: {
                    xtype: 'ctk-tasks-history-action-menu',
                    itemId: me.historyActionItemId
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('customTask.history.pagingtoolbartop.displayMsg', 'APR', '{0} - {1} of {2} history items'),
                displayMoreMsg: Uni.I18n.translate('customTask.history.pagingtoolbartop.displayMoreMsg', 'APR', '{0} - {1} of more than {2} history items'),
                emptyMsg: Uni.I18n.translate('customTask.history.pagingtoolbartop.emptyMsg', 'APR', 'There are no history items to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('customTask.history.pagingtoolbarbottom.itemsPerPage', 'APR', 'History items per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});

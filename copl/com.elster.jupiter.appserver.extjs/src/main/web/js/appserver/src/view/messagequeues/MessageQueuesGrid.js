/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.MessageQueuesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.message-queues-grid',
    store: 'Apr.store.MessageQueues',
    router: null,
    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    plugins: [
        {
            ptype: 'cellediting',
            clicksToEdit: 1,
            pluginId: 'cellplugin'
        }
    ],
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                xtype: 'uni-default-column',
                dataIndex: 'isDefault',
                flex: 0.1
            },
            {
                header: Uni.I18n.translate('general.name', 'APR', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.type', 'APR', 'Type'),
                dataIndex: 'queueTypeName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.retries', 'APR', 'Retries'),
                dataIndex: 'numberOfRetries',
                flex: 1,
                editor: {
                    xtype: 'numberfield',
                    minValue: 0,
                    maxValue: 1000
                }
            },
            {
                header: Uni.I18n.translate('general.timeBeforeRetry', 'APR', 'Time before retry'),
                dataIndex: 'retryDelayInMinutes',
                flex: 1,
                editor: {
                    xtype: 'numberfield',
                    minValue: 1,
                    maxValue: 44640
                },
                renderer: function(value){
                    return Uni.I18n.translatePlural('general.minute',value,'APR', '{0} minutes','{0} minute','{0} minutes')
                }
            },
            {
                header: Uni.I18n.translate('general.actions', 'APR', 'Actions'),
                xtype: 'uni-actioncolumn-remove',
                width: 120,
                isDisabled: function (view, rowIndex, colIndex, item, record) {
                    return !Apr.privileges.AppServer.canAdministrate() || record.get('isDefault');
                },
                handler: function (grid, rowIndex, colIndex, column, event, queueRecord) {
                    me.fireEvent('queueRemoveEvent', queueRecord);
                }

            }
        ];


        me.callParent(arguments);
    }
});

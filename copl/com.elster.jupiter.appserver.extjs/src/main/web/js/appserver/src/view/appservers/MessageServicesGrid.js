/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.MessageServicesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.message-services-grid',
    requires: [
        'Apr.view.appservers.MessageServicesActionMenu',
        'Apr.store.ActiveService',
        'Uni.grid.column.RemoveAction'
    ],
    width: '100%',
    maxHeight: 300,
    plugins: [
        'showConditionalToolTip',
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
                header: Uni.I18n.translate('general.messageService', 'APR', 'Message service'),
                dataIndex: 'messageService',
                getSortParam: Ext.emptyFn,
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'APR', 'Status'),
                dataIndex: 'active',
                flex: 0.5,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'APR', 'Active') : Uni.I18n.translate('general.inactive', 'APR', 'Inactive');
                }
            },
            {
                itemId: 'threads-column',
                header: Uni.I18n.translate('general.threads', 'APR', 'Threads'),
                dataIndex: 'numberOfThreads',
                align: 'right',
                flex: 0.5,
                emptyCellText: 1
            },
            {
                xtype: 'uni-actioncolumn-remove',
                privileges: Apr.privileges.AppServer.admin,
                itemId: 'apr-remove-message-service-column',
                handler: function (grid, rowIndex, colIndex, column, event, messageServiceRecord) {
                    me.fireEvent('msgServiceRemoveEvent', messageServiceRecord);
                }
            }
        ];

        if (Apr.privileges.AppServer.canAdministrate()) {
            me.columns[1].editor = {
                xtype: 'combobox',
                allowBlank: false,
                editable: false,
                displayField: 'displayName',
                valueField: 'active',
                queryMode: 'local',
                store: 'Apr.store.ActiveService'
            };
            me.columns[2].editor = {
                xtype: 'numberfield',
                minValue: 1,
                maxValue: 2147483647
            };
        }

        me.callParent(arguments);
    }

});
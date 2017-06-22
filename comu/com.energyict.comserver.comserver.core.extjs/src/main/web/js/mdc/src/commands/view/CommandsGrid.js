/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.commands.view.CommandsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.commands-grid',
    store: 'Mdc.commands.store.Commands',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.commands.view.CommandActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.deviceName', 'MDC', 'Device name'),
                dataIndex: 'parent',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : '<a href="#/devices/'+value.id+'">' + Ext.String.htmlEncode(value.id);
                }
            },
            {
                header: Uni.I18n.translate('general.commandName', 'MDC', 'Command name'),
                dataIndex: 'messageSpecification',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : value.name;
                }
            },
            {
                header: Uni.I18n.translate('general.commandCategory', 'MDC', 'Command category'),
                dataIndex: 'category',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : value.displayValue;
                }
            },
            {
                header: Uni.I18n.translate('general.releaseDate', 'MDC', 'Release date'),
                dataIndex: 'releaseDate',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                }
            },
            {
                header: Uni.I18n.translate('general.sentDate', 'MDC', 'Sent date'),
                dataIndex: 'sentDate',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                }
            },
            {
                header: Uni.I18n.translate('general.createdBy', 'MDC', 'Created by'),
                dataIndex: 'user',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                privileges: Mdc.privileges.DeviceCommands.executeCommands,
                menu: {
                    xtype: 'command-action-menu',
                    itemId: 'mdc-command-action-menu'
                },
                //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.allDeviceCommandPrivileges,
                isDisabled: me.fnIsDisabled
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('commands.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} commands'),
                displayMoreMsg: Uni.I18n.translate('commands.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} commands'),
                emptyMsg: Uni.I18n.translate('commands.pagingtoolbartop.emptyMsg', 'MDC', 'There are no commands to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                defaultPageSize: 100,
                itemsPerPageMsg: Uni.I18n.translate('commands.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Commands per page')
            }
        ];

        me.callParent(arguments);
    },

    fnIsDisabled: function (view, rowIndex, colIndex, item, record) {
        var status = record.get('status').value;
        return (status !== 'WAITING' && status !== 'PENDING') || !record.get('userCanAdministrate')
    }

});

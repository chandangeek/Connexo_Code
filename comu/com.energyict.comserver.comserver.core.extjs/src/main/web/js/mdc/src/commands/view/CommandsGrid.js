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
        'Mdc.view.setup.devicecommand.widget.ActionMenu'
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.deviceName', 'MDC', 'Device name'),
                dataIndex: 'parent',
                flex: 2,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : '<a href="#/devices/'+value.id+'">' + Ext.String.htmlEncode(value.id);
                }
            },
            {
                header: Uni.I18n.translate('general.commandName', 'MDC', 'Command name'),
                dataIndex: 'command',
                flex: 3,
                renderer: function (val) {
                    var res = val.name;
                    if (val.status === 'WAITING' || val.status === 'PENDING') {
                        res = (Ext.isDefined(val.willBePickedUpByPlannedComTask) && !val.willBePickedUpByPlannedComTask) ||
                              (Ext.isDefined(val.willBePickedUpByComTask) && !val.willBePickedUpByComTask)
                            ? '<span class="icon-target" style="display:inline-block; color:rgba(255, 0, 0, 0.3);"></span><span style="position:relative; left:5px;">' + val.name + '</span>'
                            : val.name;
                    }
                    return res;
                }
            },
            {
                header: Uni.I18n.translate('general.commandCategory', 'MDC', 'Command category'),
                dataIndex: 'category',
                flex: 3
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
                flex: 2,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : Uni.DateTime.formatDateTimeShort(new Date(value));
                }
            },
            {
                header: Uni.I18n.translate('general.sentDate', 'MDC', 'Sent date'),
                dataIndex: 'sentDate',
                flex: 2,
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
                    xtype: 'device-command-action-menu',
                    itemId: 'mdc-commands-grid-action-menu'
                },
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
                emptyMsg: Uni.I18n.translate('commands.pagingtoolbartop.emptyMsg', 'MDC', 'There are no commands'),
                items: [
                    {
                        xtype: 'button',
                        privileges: Mdc.privileges.DeviceCommands.executeCommands,
                        action: 'bulk',
                        itemId: 'mdc-commands-grid-add-command-btn',
                        text: Uni.I18n.translate('general.addCommand', 'MDC', 'Add command')
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                defaultPageSize: 50,
                needExtendedData: true,
                itemsPerPageMsg: Uni.I18n.translate('general.commandsPerPage', 'MDC', 'Commands per page')
            }
        ];

        me.on('afterrender', me.addTooltip);
        me.callParent(arguments);
    },

    addTooltip: function () {
        var me = this,
            view = me.getView(),
            tip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: 'span.icon-target',
                trackMouse: true,
                renderTo: Ext.getBody(),
                listeners: {
                    beforeshow: function updateTipBody(tip) {
                        var res,
                            rowEl = Ext.get(tip.triggerElement).up('tr'),
                            willBePickedUpByComTask = view.getRecord(rowEl).get('willBePickedUpByComTask'),
                            willBePickedUpByPlannedComTask = view.getRecord(rowEl).get('willBePickedUpByPlannedComTask');
                        !willBePickedUpByPlannedComTask && (res = Uni.I18n.translate('deviceCommand.willBePickedUpByPlannedComTask', 'MDC', 'This command is part of a communication task that is not planned to execute.'));
                        !willBePickedUpByComTask && (res = Uni.I18n.translate('deviceCommand.willBePickedUpByComTask', 'MDC', 'This command is not part of a communication task on this device.'));
                        tip.update(res);
                    }
                }
            });
    },

    fnIsDisabled: function (view, rowIndex, colIndex, item, record) {
        var status = record.get('status').value;
        return (status !== 'WAITING' && status !== 'PENDING') || !record.get('userCanAdministrate')
    }

});

Ext.define('Mdc.view.setup.devicecommand.DeviceCommandsGrid', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Mdc.view.setup.devicecommand.widget.ActionMenu'
    ],
    alias: 'widget.deviceCommandsGrid',
    device: null,
    initComponent: function () {
        var me = this;
        me.deviceId = me.device.get('name');
        me.columns = [
            {
                header: Uni.I18n.translate('deviceCommands.view.cmdName', 'MDC', 'Command name'),
                dataIndex: 'command',
                renderer: function (val) {
                    var res = val.name;
                    if (val.status === 'WAITING' || val.status === 'PENDING') {
                        res = (Ext.isDefined(val.willBePickedUpByPlannedComTask) && !val.willBePickedUpByPlannedComTask) ||
                              (Ext.isDefined(val.willBePickedUpByComTask) && !val.willBePickedUpByComTask)
                            ? '<span class="icon-target" style="display:inline-block; color:rgba(255, 0, 0, 0.3);"></span><span style="position:relative; left:5px;">' + val.name + '</span>'
                            : val.name;
                    }
                    return res
                },
                flex: 3
            },
            {
                header: Uni.I18n.translate('deviceCommands.view.cmdCategory', 'MDC', 'Command category'),
                dataIndex: 'category',
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val) : '-'
                },
                flex: 2
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'status',
                renderer: function (val) {
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '-'
                },
                flex: 2
            },
            {
                text: Uni.I18n.translate('deviceCommands.view.releaseDate', 'MDC', 'Release date'),
                dataIndex: 'releaseDate',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                text: Uni.I18n.translate('deviceCommands.view.sentDate', 'MDC', 'Sent date'),
                dataIndex: 'sentDate',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                header: Uni.I18n.translate('deviceCommands.view.cmdCreatedBy', 'MDC', 'Created by'),
                dataIndex: 'user',
                renderer: function (val) {
                    return val ? Ext.String.htmlEncode(val) : '-'
                },
                flex: 2
            },

            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceCommands.executeCommands,
                itemId: 'commands-action-column',
                menu: {
                    xtype: 'device-command-action-menu',
                    device: me.device,
                    deviceId: me.device.get('name')
                },
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.allDeviceCommandPrivileges,
                isDisabled: me.fnIsDisabled
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('deviceCommands.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} commands'),
                displayMoreMsg: Uni.I18n.translate('deviceCommands.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} commands'),
                emptyMsg: Uni.I18n.translate('deviceCommands.pagingtoolbartop.emptyMsg', 'MDC', 'There are no commands to display'),
                items: [
                    {
                        xtype: 'button',
                        privileges: Mdc.privileges.DeviceCommands.executeCommands,
                        text: Uni.I18n.translate('devicecommands.addCommand','MDC','Add command'),
                        itemId: 'deviceAddCommandButton',
                        deviceId: me.deviceId,
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.allDeviceCommandPrivileges
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('deviceCommands.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Commands per page')
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
})
;


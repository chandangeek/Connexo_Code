/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.communicationtask.CommunicationTaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communicationTaskGrid',
    itemId: 'communicationTaskGrid',
    deviceTypeId: null,
    deviceConfigurationId: null,
    store: 'CommunicationTaskConfigsOfDeviceConfiguration',
    scroll: false,
    requires: [
        'Uni.grid.column.Action',
        'Mdc.view.setup.communicationtask.CommunicationTaskActionMenu',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.CommunicationTaskConfigsOfDeviceConfiguration'
    ],
    viewConfig: {
        style: { overflow: 'auto', overflowX: 'hidden' },
        enableTextSelection: true
    },
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('communicationtasks.task.name', 'MDC', 'Communication task'),
                dataIndex: 'comTask',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('communicationtasks.task.securityset', 'MDC', 'Security set'),
                dataIndex: 'securityPropertySet',
                renderer: function (value) {
                    return Ext.String.htmlEncode(value.name);
                },
                fixed: true,
                flex: 3
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'suspended',
                renderer: function (value) {
                    if (value === true) {
                        return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                    }
                    return Uni.I18n.translate('general.active', 'MDC', 'Active');
                },
                fixed: true,
                flex: 3
            },
            {
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.DeviceType.admin,
                items:'Mdc.view.setup.communicationtask.CommunicationTaskActionMenu'
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('communicationtasks.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('communicationtasks.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('communicationtasks.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communication tasks to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'device-configuration-communication-task-configuration-add-btn',
                        text: Uni.I18n.translate('communicationtasks.add', 'MDC', 'Add communication task configuration'),
                        privileges: Mdc.privileges.DeviceType.admin,
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigurationId + '/comtaskenablements/add'
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('communicationtasks.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communication tasks per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});


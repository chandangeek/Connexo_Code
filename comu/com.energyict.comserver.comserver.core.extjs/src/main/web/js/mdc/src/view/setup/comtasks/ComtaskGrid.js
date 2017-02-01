/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.ComtaskGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.comtaskGrid',
    store: 'Mdc.store.CommunicationTasks',
    requires: [
        'Mdc.view.setup.comtasks.ComtaskActionMenu'
    ],
    router: null,
    forceFit: true,
    autoScroll: false,
    enableColumnHide: false,

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                sortable: false,
                menuDisabled: true,
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return '<a href="#/administration/communicationtasks/' + record.get('id') + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                itemId: 'action',
                sortable: false,
                menuDisabled: true,
                xtype: 'uni-actioncolumn',
                privileges: Mdc.privileges.Communication.admin,
                menu: {
                    xtype: 'comtaskActionMenu'
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: 'Mdc.store.CommunicationTasks',
                displayMsg: Uni.I18n.translate('comtask.display.msg', 'MDC', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('comtask.display.more.msg', 'MDC', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('comtask.empty.msg', 'MDC', '0 communication tasks'),
                dock: 'top',
                border: false,
                items: [
                    {
                        xtype: 'button',
                        itemId: 'add-communication-task',
                        text: Uni.I18n.translate('comtask.create', 'MDC', 'Add communication task'),
                        privileges: Mdc.privileges.Communication.admin,
                        action: 'createcommunicationtasks',
                        href: me.router.getRoute('administration/communicationtasks/create').buildUrl()
                    }
                ]

            },
            {
                xtype: 'pagingtoolbarbottom',
                itemsPerPageMsg: Uni.I18n.translate('comtask.items.per.page.msg', 'MDC', 'Communication tasks per page'),
                store: 'Mdc.store.CommunicationTasks',
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});
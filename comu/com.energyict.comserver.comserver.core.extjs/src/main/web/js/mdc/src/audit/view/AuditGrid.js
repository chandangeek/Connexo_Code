/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.auditGrid',
    store: 'Mdc.audit.store.Audit',
    scroll: false,
    requires: [],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('audit.change', 'MDC', 'Change'),
                dataIndex: 'operation',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return value == 'UPDATE' ? Uni.I18n.translate('audit.operation.update', 'MDC', 'Update') :
                        value == 'INSERT' ? Uni.I18n.translate('audit.operation.insert', 'MDC', 'Insert') : value;
                }
            },
            {
                header: Uni.I18n.translate('audit.change', 'MDC', 'Change on'),
                dataIndex: 'changedOn',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                header: Uni.I18n.translate('audit.category', 'MDC', 'Type'),
                dataIndex: 'category',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return value == 'DEVICE' ? Uni.I18n.translate('audit.category.device', 'MDC', 'Device') : value;
                }
            },
            {
                header: Uni.I18n.translate('audit.name', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('audit.entry', 'MDC', 'Entry'),
                dataIndex: 'subCategory',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return value == 'GENERAL_ATTRIBUTES' ? Uni.I18n.translate('audit.subCategory.generalAttributes', 'MDC', 'General attributes') : value;
                }
            },
            {
                header: Uni.I18n.translate('audit.user', 'MDC', 'User'),
                dataIndex: 'user',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('audit.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} audit trails'),
                displayMoreMsg: Uni.I18n.translate('audit.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} audit trails'),
                emptyMsg: Uni.I18n.translate('audit.pagingtoolbartop.emptyMsg', 'MDC', 'There are no audit trails to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('audit.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Audit trails per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});


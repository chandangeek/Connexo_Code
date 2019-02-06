/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.audit.view.AuditGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.auditGrid',
    store: 'Mdc.audit.store.Audit',
    requires: [],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('audit.changeOn', 'MDC', 'Change on'),
                dataIndex: 'changedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                header: Uni.I18n.translate('audit.category', 'MDC', 'Category'),
                dataIndex: 'domain',
                flex: 1
            },
            {
                header: Uni.I18n.translate('audit.name', 'MDC', 'Name'),
                dataIndex: 'auditReference',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return me.domainConvertorFn.call(me.scope, value, record);
                }
            },
            {
                header: Uni.I18n.translate('audit.entity', 'MDC', 'Entity'),
                dataIndex: 'context',
                flex: 5,
                renderer: function (value, metaData, record) {
                    return me.contextConvertorFn.call(me.scopeFn, value, record);
                }
            },
            {
                header: Uni.I18n.translate('audit.change', 'MDC', 'Change'),
                dataIndex: 'operation',
                flex: 1
            },
            {
                header: Uni.I18n.translate('audit.user', 'MDC', 'Changed by'),
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


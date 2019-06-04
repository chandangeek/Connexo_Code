/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.audit.view.AuditGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.auditGrid',
    requires: [],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('audit.changeOn', 'CFG', 'Change on'),
                dataIndex: 'changedOn',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                header: Uni.I18n.translate('audit.category', 'CFG', 'Category'),
                dataIndex: 'domain',
                flex: 1
            },
            {
                header: Uni.I18n.translate('audit.name', 'CFG', 'Name'),
                dataIndex: 'auditReference',
                flex: 2,
                renderer: function (value, metaData, record) {
                    return me.domainConvertorFn.call(me.scope, value, record);
                }
            },
            {
                header: Uni.I18n.translate('audit.entity', 'CFG', 'Entity'),
                dataIndex: 'context',
                flex: 3,
                renderer: function (value, metaData, record) {
                    return me.contextConvertorFn.call(me.scopeFn, value, record);
                }
            },
            {
                header: Uni.I18n.translate('audit.change', 'CFG', 'Change'),
                dataIndex: 'operation',
                flex: 1
            },
            {
                header: Uni.I18n.translate('audit.user', 'CFG', 'Changed by'),
                dataIndex: 'user',
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('audit.pagingtoolbartop.displayMsg', 'CFG', '{0} - {1} of {2} audit trails'),
                displayMoreMsg: Uni.I18n.translate('audit.pagingtoolbartop.displayMoreMsg', 'CFG', '{0} - {1} of more than {2} audit trails'),
                emptyMsg: Uni.I18n.translate('audit.pagingtoolbartop.emptyMsg', 'CFG', 'There are no audit trails to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('audit.pagingtoolbarbottom.itemsPerPage', 'CFG', 'Audit trails per page'),
                dock: 'bottom'
            }
        ];
        me.callParent(arguments);
    }
});


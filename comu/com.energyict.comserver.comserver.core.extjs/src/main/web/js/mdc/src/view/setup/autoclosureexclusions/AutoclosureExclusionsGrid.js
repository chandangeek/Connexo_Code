/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.autoclosureexclusions.AutoclosureExclusionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.autoclosure-exclusions-grid',
    overflowY: 'auto',
    store: 'Mdc.store.AutoclosureExclusions',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.AutoclosureExclusions',
        'Uni.grid.column.Default'
    ],
    device: null,


    initComponent: function () {
        var me = this;
        me.deviceId = me.device.get('name');
        me.columns = [
            {
                header: Uni.I18n.translate('creationRule.ruleName', 'MDC', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
			{
                header: Uni.I18n.translate('creationRule.ruleTemplate', 'MDC', 'Rule template'),
                dataIndex: 'template_name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('creationRule.issueType', 'MDC', 'Issue type'),
                dataIndex: 'issueType_name',
                flex: 1
            },
			{
                itemId: 'statusColumn',
                header: Uni.I18n.translate('creationRule.status', 'MDC', 'Status'),
                dataIndex: 'active',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('creationRules.active', 'MDC', 'Active')
                        : Uni.I18n.translate('creationRules.inactive', 'MDC', 'Inactive');
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('creationRules.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} creation rules'),
                displayMoreMsg: Uni.I18n.translate('creationRules.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} creation rules'),
                emptyMsg: Uni.I18n.translate('creationRules.pagingtoolbartop.emptyMsg', 'MDC', 'There are no creation rules to display'),
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                itemsPerPageMsg: Uni.I18n.translate('creationRules.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Creation rules per page'),
            }
        ];

        me.callParent();
    }
});




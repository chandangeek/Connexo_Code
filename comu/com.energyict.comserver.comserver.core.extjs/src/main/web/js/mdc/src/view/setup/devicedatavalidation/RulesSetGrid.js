/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataValidationRulesSetGrid',
    itemId: 'deviceDataValidationRulesSetGrid',
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesSetActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'DeviceDataValidationRulesSet',
    overflowY: 'auto',
    deviceId: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.name', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    if (record.raw.description) {
                        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode( Ext.String.htmlEncode(record.raw.description)) + '"';
                    }
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 6
            },

            {
                header: Uni.I18n.translate('validation.activeVersion', 'MDC', 'Active version'),
                dataIndex: 'activeVersion',
                flex: 6,
                align: 'left',
                sortable: false,
                fixed: true
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'isActive',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.active', 'MDC', 'Active')
                        : Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                },
                flex: 2,
                align: 'left',
                sortable: false,
                fixed: true
            },
            {
                xtype: 'uni-actioncolumn',
                privileges:Cfg.privileges.Validation.device,
                menu: {
                    xtype: 'deviceDataValidationRulesSetActionMenu'
                },
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationRuleSetsActions
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.displayMsgRuleSet', 'MDC', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.displayMoreMsgRuleSet', 'MDC', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.emptyMsgRuleSet', 'MDC', 'There are no validation rule sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.bottom.itemsPerPageRuleSet', 'MDC', 'Validation rule sets per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.validation.RulesSetGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validationConfigurationRulesSetGrid',
    itemId: 'validationConfigurationRulesSetGrid',
    requires: [
        'Imt.purpose.view.summary.validation.RulesSetActionMenu',
        'Imt.purpose.store.PurposeValidationConfiguration',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Imt.purpose.store.PurposeValidationConfiguration',
    overflowY: 'auto',
    deviceId: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.name', 'IMT', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    if (record.raw.description) {
                        metaData.tdAttr = 'data-qtip="' + Ext.String.htmlEncode(Ext.String.htmlEncode(record.raw.description)) + '"';
                    }
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 6
            },

            {
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'activeVersion',
                flex: 6,
                align: 'left',
                sortable: false,
                fixed: true
            },
            {
                header: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'isActive',
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.active', 'IMT', 'Active')
                        : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive');
                },
                flex: 2,
                align: 'left',
                sortable: false,
                fixed: true
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                //privileges:Cfg.privileges.Validation.device,
                menu: {
                    xtype: 'validationRulesSetActionMenu'
                },
                //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationRuleSetsActions
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.displayMsgRuleSet', 'IMT', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.displayMoreMsgRuleSet', 'IMT', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.emptyMsgRuleSet', 'IMT', 'There are no validation rule sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.bottom.itemsPerPageRuleSet', 'IMT', 'Validation rule sets per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
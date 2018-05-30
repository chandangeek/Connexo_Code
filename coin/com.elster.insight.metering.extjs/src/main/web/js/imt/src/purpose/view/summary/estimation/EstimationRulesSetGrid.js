/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.summary.estimation.EstimationRulesSetGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.estimationCfgRulesSetGrid',
    itemId: 'estimationCfgRulesSetGrid',
    requires: [
        'Imt.purpose.view.summary.estimation.EstimationRulesSetActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Imt.purpose.store.PurposeEstimationConfiguration',
    overflowY: 'auto',
    deviceId: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('estimationPurpose.estimationRuleSet', 'IMT', 'Estimation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    return '<a href="#/administration/estimationrulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'isActive',
                flex: 1,
                renderer: function (value) {
                    return value
                        ? Uni.I18n.translate('general.active', 'IMT', 'Active')
                        : Uni.I18n.translate('general.inactive', 'IMT', 'Inactive');
                },
                align: 'right',
                sortable: false,
                fixed: true
            },
            {
                header: Uni.I18n.translate('general.activeRules', 'IMT', 'Active rules'),
                dataIndex: 'activeRules',
                align: 'right',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.inactiveRules', 'IMT', 'Inactive rules'),
                dataIndex: 'inactiveRules',
                align: 'right',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                width: 120,
                menu: {
                    xtype: 'estimationCfgRulesSetActionMenu'
                },
                //privileges: Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfigurationOnDevice,
                //dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.estimationRuleSetsActions
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('estimationPurpose.rulesSetGrid.pgtbar.top.displayMsgRuleSet', 'IMT', '{0} - {1} of {2} estimation rule sets'),
                displayMoreMsg: Uni.I18n.translate('estimationPurpose.rulesSetGrid.pgtbar.top.displayMoreMsgRuleSet', 'IMT', '{0} - {1} of more than {2} estimation rule sets'),
                emptyMsg: Uni.I18n.translate('estimationPurpose.rulesSetGrid.pgtbar.top.emptyMsgRuleSet', 'IMT', 'There are no estimation rule sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationPurpose.rulesSetGrid.pgtbar.bottom.itemsPerPageRuleSet', 'IMT', 'Estimation rule sets per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
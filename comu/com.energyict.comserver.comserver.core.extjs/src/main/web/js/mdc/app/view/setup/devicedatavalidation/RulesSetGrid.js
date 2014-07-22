Ext.define('Mdc.view.setup.devicedatavalidation.RulesSetGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataValidationRulesSetGrid',
    itemId: 'deviceDataValidationRulesSetGrid',
    ui: 'medium',
    padding: '20 0',
    title: Uni.I18n.translate('device.dataValidation.rulesSetGrid.title', 'MDC', 'Validation rule sets'),
    requires: [
        'Mdc.view.setup.devicedatavalidation.RulesSetActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'DeviceDataValidationRulesSet',
    mRID: null,
    overflowY: 'auto',
    initComponent: function () {
        this.columns = [
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.name', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + value + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.status', 'MDC', 'Status'),
                dataIndex: 'status',
                align: 'center',
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.activeRules', 'MDC', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                align: 'center',
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.inactiveRules', 'MDC', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                align: 'center',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.devicedatavalidation.RulesSetActionMenu'
            }
        ];
        this.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: this.store,
                displayMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.displayMsgRuleSet', 'MDC', '{0} - {1} of {2} Validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.displayMoreMsgRuleSet', 'MDC', '{0} - {1} of more than {2} Validation rule sets'),
                emptyMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.top.emptyMsgRuleSet', 'MDC', 'There are no validation rule sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                dock: 'bottom',
                store: this.store,
                itemsPerPageMsg: Uni.I18n.translate('device.dataValidation.rulesSetGrid.pgtbar.bottom.itemsPerPageRuleSet', 'MDC', 'Validation rule sets per page')
            }
        ];
        this.callParent();
    }
});


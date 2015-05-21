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
    mRID: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.name', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    if (record.raw.description) {
                        metaData.tdAttr = 'data-qtip="' + record.raw.description + '"';
                    }
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.status', 'MDC', 'Status'),
                dataIndex: 'isActive',
                align: 'center',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            },
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.activeRules', 'MDC', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                align: 'right',
                flex: 1
            },
            {
                header: Uni.I18n.translate('device.dataValidation.rulesSetGrid.columnHeader.inactiveRules', 'MDC', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                align: 'right',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                privileges:Cfg.privileges.Validation.device,
                items: 'Mdc.view.setup.devicedatavalidation.RulesSetActionMenu'
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
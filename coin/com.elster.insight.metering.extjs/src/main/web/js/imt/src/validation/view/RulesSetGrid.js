Ext.define('Imt.validation.view.RulesSetGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagePointDataValidationRulesSetGrid',
    itemId: 'usagePointDataValidationRulesSetGrid',
    requires: [
        'Imt.validation.view.RulesSetActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Imt.validation.store.UsagePointDataValidationRulesSet'
        
    ],
    store: 'Imt.validation.store.UsagePointDataValidationRulesSet',
    overflowY: 'auto',
    mRID: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.columnHeader.name', 'IMT', 'Validation rule set'),
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
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'version', //'activeVersion',
                flex: 8,
                align: 'left',
                sortable: false,
                fixed: true
            },	        
            {
                xtype: 'uni-actioncolumn',
                flex: 2,
               // privileges:Cfg.privileges.Validation.device,
                items: 'Imt.validation.view.RulesSetActionMenu',
               // dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationRuleSetsActions
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.pgtbar.top.displayMsgRuleSet', 'IMT', '{0} - {1} of {2} validation rule sets'),
                displayMoreMsg: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.pgtbar.top.displayMoreMsgRuleSet', 'IMT', '{0} - {1} of more than {2} validation rule sets'),
                emptyMsg: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.pgtbar.top.emptyMsgRuleSet', 'IMT', 'There are no validation rule sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('usagepoint.dataValidation.rulesSetGrid.pgtbar.bottom.itemsPerPageRuleSet', 'IMT', 'Validation rule sets per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
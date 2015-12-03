Ext.define('Imt.validation.view.RulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagePointDataValidationRulesGrid',
    itemId: 'usagePointDataValidationRulesGrid',
    ruleSetId: null,
    versionId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Cfg.store.ValidationRules'
    ],
    store: 'ValidationRules',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validation.ruleName', 'IMT', 'Validation rule'),
                dataIndex: 'name',
                flex: 6,
                renderer: function (value, b, record) {
                    /*
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId')
                        + '/rules/' + record.getId() + '">' + value + '</a>';
                    */
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetVersion').ruleSet.id + '/versions/' + record.get('ruleSetVersion').id + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'IMT', 'Status'),
                dataIndex: 'active',
                flex: 10,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.active', 'IMT', 'Active')
                    } else {
                        return Uni.I18n.translate('general.inactive', 'IMT', 'Inactive')
                    }
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                usesExactCount: true,
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRule', 'IMT', '{0} - {1} of {2} validation rules'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRule', 'IMT', '{0} - {1} of more than {2} validation rules'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRule', 'IMT', 'There are no validation rules to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                isSecondPagination: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRule', 'IMT', 'Validation rules per page'),
                dock: 'bottom',
                params: {
                    ruleSetId: me.ruleSetId,
                    versionId: me.versionId
                },
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
Ext.define('Mdc.view.setup.devicedatavalidation.RulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataValidationRulesGrid',
    itemId: 'deviceDataValidationRulesGrid',
    rulesSetId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Cfg.store.ValidationRules',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validation.ruleName', 'MDC', 'Validation rule'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId')
                        + '/rules/' + record.getId() + '">' + value + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('validation.status', 'CFG', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                    } else {
                        return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
                    }
                }
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMsgRule', 'MDC', '{0} - {1} of {2} validation rules'),
                displayMoreMsg: Uni.I18n.translate('validation.pagingtoolbartop.displayMoreMsgRule', 'MDC', '{0} - {1} of more than {2} validation rules'),
                emptyMsg: Uni.I18n.translate('validation.pagingtoolbartop.emptyMsgRule', 'MDC', 'There are no validation rules to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRule', 'MDC', 'Validation rules per page'),
                dock: 'bottom',
                params: {id: me.rulesSetId},
                deferLoading: true,
                pageSizeParam: 'limit2',
                pageStartParam: 'start2'
            }
        ];
        me.callParent(arguments);
    }
});
Ext.define('Mdc.view.setup.registerconfig.RulesForRegisterConfigGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'validation-rules-for-registerconfig-grid',
    itemId: 'rulesForRegisterConfigGrid',
    overflowY: 'auto',

    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Mdc.store.RegisterConfigValidationRules',
        'Mdc.view.setup.registerconfig.RulesForRegisterConfigActionMenu'
    ],

    store: 'RegisterConfigValidationRules',

    deviceTypeId: null,
    deviceConfigId: null,
    registerConfigId: null,

    initComponent: function () {
        var me = this;

        this.columns = [
            {
                header: Uni.I18n.translate('validation.validationRule', 'MDC', 'Validation rule'),
                dataIndex: 'name',
                renderer: function (value, b, record) {
                    return '<a href="#/administration/validation/rulesets/' + record.get('ruleSetId') 
                        + '/versions/' + record.get('ruleSetVersionId') 
                        + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 10
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'), dataIndex: 'active', flex: 3, sortable: false, fixed: true,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.active', 'MDC', 'Active')
                    } else {
                        return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                    }
                }
            },
            {
                header: Uni.I18n.translate('validation.validationRuleSet', 'MDC', 'Validation rule set'),
                dataIndex: 'ruleSet',
                renderer: function (value, metaData, record) {
                   var ruleSetVersion = record.get('ruleSetVersion');
					if (ruleSetVersion){
						var ruleSet = ruleSetVersion.ruleSet;
						if (ruleSet){
							metaData.tdAttr = 'data-qtip="' + ruleSet.description + '"';
							return '<a href="#/administration/validation/rulesets/' + ruleSet.id + '">' + Ext.String.htmlEncode(ruleSet.name) + '</a>';
						}
					}
                    
                    return '';
                },
                flex: 10
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.registerconfig.RulesForRegisterConfigActionMenu'
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
                params: {
                    deviceType: me.deviceTypeId,
                    deviceConfig: me.deviceConfigId,
                    registerConfig: me.registerConfigId
                },
                pageSizeParam: 'limit2',
                pageStartParam: 'start2',
                deferLoading: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('validation.pagingtoolbarbottom.itemsPerPageRule', 'MDC', 'Validation rules per page'),
                dock: 'bottom'
            }
        ];

        me.callParent();
    }
});


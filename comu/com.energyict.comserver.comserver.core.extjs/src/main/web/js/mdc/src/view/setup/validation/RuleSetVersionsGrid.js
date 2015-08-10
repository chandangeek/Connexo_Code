Ext.define('Mdc.view.setup.validation.RuleSetVersionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.validation-versions-grid',
    rulesSetId: null,
	versionId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Cfg.store.ValidationRuleSetVersions',
		'Mdc.view.setup.validation.VersionActionMenu'
    ],
    store: 'Cfg.store.ValidationRuleSetVersions',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validation.period', 'MDC', 'Period'),
                dataIndex: 'versionName',
                flex: 3,
                sortable: false,
                fixed: true,
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = Ext.String.htmlEncode(Ext.String.htmlEncode('data-qtip="' + record.get('description').replace(/(?:\r\n|\r|\n)/g, '<br />') + '"'));
                    return value;
                }
            },
            {
                header: Uni.I18n.translate('validation.versionDescription', 'MDC', 'Description'),
                dataIndex: 'description',
                flex: 3,
                align: 'left',
                sortable: false,
                fixed: true
            },
			{
                header: Uni.I18n.translate('validation.activeRules', 'MDC', 'Active rules'),
                dataIndex: 'numberOfRules',
				align: 'left',
                flex: 1,
                renderer: function (value, b, record) {
                    var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
                    return numberOfActiveRules;
                }
            },
            {
                header: Uni.I18n.translate('validation.inactiveRules', 'MDC', 'Inactive rules'),
                align: 'left',
                dataIndex: 'numberOfInactiveRules',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.validation.VersionActionMenu'
            }
        ]; 
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('validation.version.display.msg', 'MDC', '{0} - {1} of {2} versions'),
                displayMoreMsg: Uni.I18n.translate('validation.version.display.more.msg', 'MDC', '{0} - {1} of more than {2} versions'),
                emptyMsg: Uni.I18n.translate('validation.version.pagingtoolbartop.emptyMsg', 'MDC', 'There are no versions to display'),
                dock: 'top'/*,
                items: [
                    {
                        text: Uni.I18n.translate('validation.addVersion', 'CFG', 'Add version'),
                        privileges: Cfg.privileges.Validation.admin,
                        itemId: 'newVersion',
                        xtype: 'button',
                        href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/add',
                        hrefTarget: '_self'
                    }
                ]*/
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
				deferLoading: true,
                itemsPerPageMsg: 'Versions per page',
                dock: 'bottom',
                isSecondPagination: me.isSecondPagination,
                params: {ruleSetId: me.ruleSetId}

            }
        ];
	    me.listeners = {
            'afterrender': function (grid) {
                grid.getStore().on('load', function(store, records, success) {
                    var rec = store.find('status', 'CURRENT');
                    if ((rec>=0)|| (grid.getView())) {
                        Ext.Function.defer(function(){

                            grid.getView().select(rec);
                        }, 10);
                    }

                }, grid, {
                    single: true
                });
            }
        };


        me.callParent(arguments);
    },
	updateValidationVersionSet: function (record) {      
		 var me = this,
            grid = me.up('validation-ruleset-view').down('validation-rules-grid'),
            addButton = me.up('validation-ruleset-view').down('button[action=addValidationRule]');
			
        me.setTitle(record.get('name'));
        me.validationRuleSetId = record.get('ruleSetId');		
		me.versionId = record.get('versionId');
		addButton.setHref('#/administration/validation/rulesets/' + record.get('ruleSetId') + '/versions/' + record.get('id') + '/rules/add');        
        grid.updateValidationRuleSetId(me.validationRuleSetId);

        grid.store.load({params: {
            ruleSetId: record.get('ruleSetId'),
			versionId: record.get('id')
        }});
    }

});
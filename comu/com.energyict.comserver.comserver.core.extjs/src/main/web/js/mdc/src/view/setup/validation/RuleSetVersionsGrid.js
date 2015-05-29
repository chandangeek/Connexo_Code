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
                header: Uni.I18n.translate('validation.period', 'CFG', 'Period'),
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
                header: Uni.I18n.translate('validation.versionDescription', 'CFG', 'Description'),
                dataIndex: 'description',
                flex: 3,
                align: 'left',
                sortable: false,
                fixed: true
            },
			{
                header: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),                
                dataIndex: 'numberOfRules',
				align: 'left',
                flex: 1,
                renderer: function (value, b, record) {
                    var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
                    return numberOfActiveRules;
                }
            },
            {
                header: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
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
                displayMsg: Uni.I18n.translate('validation.version.display.msg', 'CFG', '{0} - {1} of {2} versions'),
                displayMoreMsg: Uni.I18n.translate('validation.version.display.more.msg', 'CFG', '{0} - {1} of more than {2} versions'),
                emptyMsg: Uni.I18n.translate('validation.version.pagingtoolbartop.emptyMsg', 'CFG', 'There are no versions to display'),
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
                        grid.getView().getSelectionModel().select(rec);
                    }

                }, grid, {
                    single: true
                });
            }
        };


        me.callParent(arguments);
    },
	updateValidationRuleSetId: function (validationRuleSetId) {      
		 var me = this,
            grid = me.down('validation-rules-grid'),
            addButton = me.down('button[action=addValidationRule]');
			
        me.setTitle(record.get('name'));
        me.validationRuleSetId = record.get('id');		
		me.versionId = record.get('versionId');
        addButton.setHref('#/administration/validation/rulesets/' + me.validationRuleSetId + '/rules/add');
        grid.updateValidationRuleSetId(me.validationRuleSetId);

        grid.store.load({params: {
            id: me.validationRuleSetId,
			versionId: me.versionId
        }});
    }

});
Ext.define('Imt.validation.view.RuleSetVersionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.usagePointDataValidationRuleSetVersionsGrid',
    itemId: 'usagePointDataValidationRuleSetVersionsGrid',
    rulesSetId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Cfg.store.ValidationRuleSetVersions'
    ],
    store: 'ValidationRuleSetVersions',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validation.period', 'IMT', 'Period'),
                dataIndex: 'versionName',
                flex: 6,
                sortable: false,
                fixed: true,
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = Ext.String.htmlEncode(Ext.String.htmlEncode('data-qtip="' + record.get('description').replace(/(?:\r\n|\r|\n)/g, '<br />') + '"'));
                    return value;
                }
            },
            {
                header: Uni.I18n.translate('validation.versionDescription', 'IMT', 'Description'),
                dataIndex: 'description',
                flex: 6,
                align: 'left',
                sortable: false,
                fixed: true
            },
			{
                header: Uni.I18n.translate('validation.activeRules', 'IMT', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
				align: 'left',
                flex: 2,
                fixed: true
            },
            {
                header: Uni.I18n.translate('validation.inactiveRules', 'IMT', 'Inactive rules'),
                align: 'left',
                dataIndex: 'numberOfInactiveRules',
                flex: 2,
                fixed: true
            }
        ]; 
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('validation.version.display.msg', 'IMT', '{0} - {1} of {2} versions'),
                displayMoreMsg: Uni.I18n.translate('validation.version.display.more.msg', 'IMT', '{0} - {1} of more than {2} versions'),
                emptyMsg: Uni.I18n.translate('validation.version.pagingtoolbartop.emptyMsg', 'IMT', 'There are no versions to display'),
                dock: 'top'               
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
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
                        //grid.getView().getSelectionModel().select(rec);
                    	grid.getSelectionModel().select(rec);
                    }

                }, grid, {
                    single: true
                });
            }
        };


        me.callParent(arguments);
    }
});
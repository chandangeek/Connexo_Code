Ext.define('Cfg.view.validation.VersionsList', {
    extend: 'Ext.grid.Panel',
    border: true,
    alias: 'widget.versionsList',
    itemId: 'versionsList',
    store: 'ValidationRuleSetVersions',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Action',
        'Cfg.view.validation.VersionsActionMenu',
        'Uni.I18n',
        'Uni.Auth',
        'Ext.button.Button'
    ],
    ruleSetId: null,
	versionId: null,
    columns: {
        items: [
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
                header: Uni.I18n.translate('general.description', 'CFG', 'Description'),
                dataIndex: 'description',
                flex: 3,
                align: 'left',
                sortable: false,
                fixed: true
            },			
			{
                header: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                flex: 1,
                align: 'left',
                sortable: false,
                fixed: true,
                renderer: function (value, b, record) {
                    var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
                    return numberOfActiveRules;
                }
            },			
			{
                header: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                flex: 1,
                align: 'left',
                sortable: false,
                fixed: true
            },	
            {
                xtype: 'uni-actioncolumn',				
                privileges: Cfg.privileges.Validation.admin,
                menu: {
                    itemId: 'ruleSetVersionsGridMenu',
                    xtype: 'versions-action-menu'
                }

            }
        ]
    },

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                displayMsg: Uni.I18n.translate('validation.version.display.msg', 'CFG', '{0} - {1} of {2} versions'),
                displayMoreMsg: Uni.I18n.translate('validation.version.display.more.msg', 'CFG', '{0} - {1} of more than {2} versions'),
                emptyMsg: Uni.I18n.translate('validation.version.pagingtoolbartop.emptyMsg', 'CFG', 'There are no versions to display'),
                dock: 'top',
                items: [
                    {
                        text: Uni.I18n.translate('validation.addVersion', 'CFG', 'Add version'),
                        privileges: Cfg.privileges.Validation.admin,
                        itemId: 'newVersion',
                        xtype: 'button',
                        href: '#/administration/validation/rulesets/' + me.ruleSetId + '/versions/add',
                        hrefTarget: '_self'
                    }
                ]
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
            'afterrender': function (component) {
                component.getStore().on('load', function(store, records, success) {
                    var rec = store.find('status', 'CURRENT');
                    if ((rec>=0)|| (this.getView())) {
                        this.getView().getSelectionModel().select(rec);
                    }

                }, this, {
                    single: true
                });
            }
        };

        me.callParent(arguments);
    }

});

Ext.define('Mdc.view.setup.devicedatavalidation.RuleSetVersionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataValidationRuleSetVersionsGrid',
    itemId: 'deviceDataValidationRuleSetVersionsGrid',
    rulesSetId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Cfg.view.validation.VersionsActionMenu'
    ],
    store: 'Cfg.store.ValidationRuleSetVersions',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('validation.period', 'CFG', 'Period'),
                dataIndex: 'versionName',
                flex: 0.3,
                sortable: false,
                fixed: true,
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description').replace(/(?:\r\n|\r|\n)/g, '<br />') + '"';
                    return value;
                }
            },
            {
                header: Uni.I18n.translate('validation.versionDescription', 'CFG', 'Description'),
                dataIndex: 'description',
                flex: 0.3,
                align: 'left',
                sortable: false,
                fixed: true
            }
        ];
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
                        hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
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
Ext.define('Mdc.view.setup.devicedatavalidation.RuleSetVersionsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataValidationRuleSetVersionsGrid',
    itemId: 'deviceDataValidationRuleSetVersionsGrid',
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

        me.callParent(arguments);
    }
});
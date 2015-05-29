Ext.define('Mdc.deviceconfigurationestimationrules.view.RulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.device-configuration-estimation-rules-grid',
    store: 'Mdc.deviceconfigurationestimationrules.store.EstimationRules',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.estimationrule', 'MDC', 'Estimation rule'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, meta, record) {
                    var res = '';
                    if (value && record && record.get('id') && record.get('ruleSet').id) {
                        var url = me.router.getRoute('administration/estimationrulesets/estimationruleset/rules/rule').buildUrl({ruleSetId: record.get('ruleSet').id, ruleId: record.get('id') });
                        res = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                    return res;
                }
            },
            {
                header: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.active', 'MDC', 'Active') :  Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('estimationrules.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} estimation rules'),
                displayMoreMsg: Uni.I18n.translate('estimationrules.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} estimation rules'),
                emptyMsg: Uni.I18n.translate('estimationrules.pagingtoolbartop.emptyMsg', 'MDC', 'There are no estimation rules to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationrules.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Estimation rules per page'),
                dock: 'bottom'
            }
        ];

        me.callParent(arguments);
    }
});
Ext.define('Mdc.view.setup.devicedataestimation.RulesGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataEstimationRulesGrid',
    rulesSetId: null,
    title: '',
    requires: [
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop',
        'Mdc.store.EstimationRules'
    ],
    store: 'Mdc.store.EstimationRules',
    overflowY: 'auto',
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('estimationDevice.estimationRule', 'MDC', 'Estimation rule'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, b, record) {
                    return '<a href="#/administration/estimationrulesets/' + record.get('ruleSet').id
                        + '/rules/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('estimationDeviceConfigurations.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    if (value) {
                        return Uni.I18n.translate('general.active', 'MDC', 'Active')
                    } else {
                        return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
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
                displayMsg: Uni.I18n.translate('estimationDevice.pagingtoolbartop.displayMsgRule', 'MDC', '{0} - {1} of {2} estimation rules'),
                displayMoreMsg: Uni.I18n.translate('estimationDevice.pagingtoolbartop.displayMoreMsgRule', 'MDC', '{0} - {1} of more than {2} estimation rules'),
                emptyMsg: Uni.I18n.translate('estimationDevice.pagingtoolbartop.emptyMsgRule', 'MDC', 'There are no estimation rules to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                isSecondPagination: true,
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationDevice.pagingtoolbarbottom.itemsPerPageRule', 'MDC', 'Estimation rules per page'),
                dock: 'bottom',
                params: {id: me.rulesSetId},
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
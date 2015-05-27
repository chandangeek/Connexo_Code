Ext.define('Mdc.view.setup.devicedataestimation.RulesSetGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceDataEstimationRulesSetGrid',
    requires: [
        'Mdc.view.setup.devicedataestimation.RulesSetActionMenu',
        'Uni.view.toolbar.PagingBottom',
        'Uni.view.toolbar.PagingTop'
    ],
    store: 'Mdc.store.DeviceDataEstimationRulesSet',
    overflowY: 'auto',
    mRID: null,
    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('estimationDevice.estimationRuleSet', 'MDC', 'Estimation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    return '<a href="#/administration/estimationrulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('estimationDeviceConfigurations.status', 'MDC', 'Status'),
                dataIndex: 'active',
                flex: 1,
                renderer: function (value) {
                    return value ?
                        Uni.I18n.translate('general.active', 'MDC', 'Active') :
                        Uni.I18n.translate('general.inactive', 'MDC', 'Inactive');
                }
            },
            {
                header: Uni.I18n.translate('general.activeRules', 'MDC', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                align: 'right',
                flex: 1
            },
            {
                header: Uni.I18n.translate('general.inactiveRules', 'MDC', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                align: 'right',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.devicedataestimation.RulesSetActionMenu',
                privileges: Mdc.privileges.DeviceConfigurationEstimations.viewfineTuneEstimationConfigurationOnDevice
            }
        ];
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('estimationDevice.rulesSetGrid.pgtbar.top.displayMsgRuleSet', 'MDC', '{0} - {1} of {2} estimation rule sets'),
                displayMoreMsg: Uni.I18n.translate('estimationDevice.rulesSetGrid.pgtbar.top.displayMoreMsgRuleSet', 'MDC', '{0} - {1} of more than {2} estimation rule sets'),
                emptyMsg: Uni.I18n.translate('estimationDevice.rulesSetGrid.pgtbar.top.emptyMsgRuleSet', 'MDC', 'There are no estimation rule sets to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('estimationDevice.rulesSetGrid.pgtbar.bottom.itemsPerPageRuleSet', 'MDC', 'Estimation rule sets per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];
        me.callParent(arguments);
    }
});
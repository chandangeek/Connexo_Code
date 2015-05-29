Ext.define('Mdc.deviceconfigurationestimationrules.view.RuleSetsBulkSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    alias: 'widget.device-configuration-estimation-add-rule-sets-bulk-selection',
    itemId: 'device-configuration-estimation-add-rule-sets-bulk-selection',
    store: 'Mdc.deviceconfigurationestimationrules.store.BulkEstimationRuleSets',

    requires: [
        'Mdc.view.setup.validation.AddRuleSetActionMenu'
    ],

    router: null,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'deviceconfiguration.estimation.ruleSets.selectedcount',
            count,
            'MDC',
            '{0} estimation rule sets selected'
        );
    },

    allLabel: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.all', 'MDC', 'All estimation rule sets'),
    allDescription: Uni.I18n.translate(
        'deviceconfiguration.estimation.ruleSets.selectall',
        'MDC',
        'Select all estimation rule sets related to device configuration'
    ),

    selectedLabel: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.selected', 'MDC', 'Selected estimation rule sets'),
    selectedDescription: Uni.I18n.translate(
        'deviceconfiguration.estimation.ruleSets.select',
        'MDC',
        'Select estimation rule sets in table'
    ),

    allChosenByDefault: true,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.estimationruleset', 'MDC', 'Estimation rule set'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, meta, record) {
                    var res = '';
                    if (value && record && record.get('id')) {
                        var url = me.router.getRoute('administration/estimationrulesets/estimationruleset').buildUrl({ruleSetId: record.get('id')});
                        res = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>'
                    }
                    return res;
                }
            },
            {
                header: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.activeRules', 'MDC', 'Active rules'),
                dataIndex: 'numberOfActiveRules',
                flex: 1
            },
            {
                header: Uni.I18n.translate('deviceconfiguration.estimation.ruleSets.inactiveRules', 'MDC', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                flex: 1
            }
        ];

        me.cancelHref = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/estimationrulesets').buildUrl();
        me.callParent(arguments);
    }
});


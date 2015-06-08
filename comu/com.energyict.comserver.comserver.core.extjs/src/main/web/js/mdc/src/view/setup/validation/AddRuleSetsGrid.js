Ext.define('Mdc.view.setup.validation.AddRuleSetsGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'validation-add-rulesets-grid',

    requires: [
        'Mdc.view.setup.validation.AddRuleSetActionMenu',
        'Mdc.store.ValidationRuleSetsForDeviceConfig'
    ],

    store: 'Mdc.store.ValidationRuleSetsForDeviceConfig',

    deviceTypeId: null,
    deviceConfigId: null,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'validation.noValidationRuleSetSelected',
            count,
            'MDC',
            '{0} validation rule sets selected'
        );
    },

    allLabel: Uni.I18n.translate('ruleset.allRuleSets', 'MDC', 'All validation rule sets'),
    allDescription: Uni.I18n.translate(
        'ruleset.selectAllRuleSets',
        'MDC',
        'Select all validation rule sets related to device configuration'
    ),

    selectedLabel: Uni.I18n.translate('ruleset.selectedRuleSets', 'MDC', 'Selected validation rule sets'),
    selectedDescription: Uni.I18n.translate(
        'ruleset.selectRuleSets',
        'MDC',
        'Select validation rule sets in table'
    ),

    allChosenByDefault: false,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'MDC', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules'),
                dataIndex: 'numberOfRules',
                align: 'right',
                flex: 1,
                renderer: function (value, b, record) {
                    var numberOfActiveRules = record.get('numberOfRules') - record.get('numberOfInactiveRules');
                    return numberOfActiveRules;
                }
            },
            {
                header: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules'),
                dataIndex: 'numberOfInactiveRules',
                align: 'right',
                flex: 1
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Mdc.view.setup.validation.AddRuleSetActionMenu'
            }
        ];

        me.cancelHref = '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/validationrulesets';
        me.callParent(arguments);
    }
});


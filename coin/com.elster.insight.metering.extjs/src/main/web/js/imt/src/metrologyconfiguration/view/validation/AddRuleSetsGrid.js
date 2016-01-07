Ext.define('Imt.metrologyconfiguration.view.validation.AddRuleSetsGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'validation-add-rulesets-grid',

    requires: [
        'Imt.metrologyconfiguration.view.validation.AddRuleSetActionMenu',
        'Imt.metrologyconfiguration.store.LinkableValidationRulesSet'
    ],

    store: 'Imt.metrologyconfiguration.store.LinkableValidationRulesSet',

    mcid: null,

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural(
            'general.nrOfValidationRuleSets.selected', count, 'IMT',
            'No validation rule sets selected', '{0} validation rule set selected', '{0} validation rule sets selected'
        );
    },

    allLabel: Uni.I18n.translate('ruleset.allRuleSets', 'IMT', 'All validation rule sets'),
    allDescription: Uni.I18n.translate('ruleset.selectAllRuleSets','IMT','Select all validation rule sets related to device configuration'),

    selectedLabel: Uni.I18n.translate('ruleset.selectedRuleSets', 'IMT', 'Selected validation rule sets'),
    selectedDescription: Uni.I18n.translate('ruleset.selectRuleSets','IMT','Select validation rule sets in table'),

    gridHeight: undefined,
    gridHeaderHeight: undefined,

    allChosenByDefault: false,

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('validation.ruleSetName', 'IMT', 'Validation rule set'),
                dataIndex: 'name',
                renderer: function (value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + record.get('description') + '"';
                    return '<a href="#/administration/validation/rulesets/' + record.getId() + '">' + Ext.String.htmlEncode(value) + '</a>';
                },
                flex: 3
            },
            {
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'activeVersion',
                flex: 5,
                align: 'left',
                sortable: false,
                fixed: true
            },
            {
                xtype: 'uni-actioncolumn',
                items: 'Imt.metrologyconfiguration.view.validation.AddRuleSetActionMenu'
            }
        ];

        me.cancelHref = '#/administration/metrologyconfiguration/' + me.mcid + '/associatedvalidationrulesets';
        me.callParent(arguments);
    },

    onChangeSelectionGroupType: function (radiogroup, value) {
        var me = this;
        if (me.view) {
            var selection = me.view.getSelectionModel().getSelection();

            me.up('validation-add-rulesets').down('validation-ruleset-view').setVisible(!me.isAllSelected() && selection.length !== 0);
            this.callParent(arguments);
        }
    }
});


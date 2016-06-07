Ext.define('Imt.metrologyconfiguration.view.validation.AddValidationRuleSetsToPurposeGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.add-validation-rule-sets-to-purpose-grid',
   
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('validationRuleSets.count.selected', count, 'IMT', 'No validation rule sets selected',
            '{0} validation rule set selected',
            '{0} validation rule sets selected');
    },   

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 1
            },
            {
                header: Uni.I18n.translate('validation.activeVersion', 'IMT', 'Active version'),
                dataIndex: 'currentVersion',
                flex: 1
            }
        ];        

        me.callParent(arguments);
    }
});

Ext.define('Imt.validation.store.UsagePointDataValidationRulesSet', {
    extend: 'Ext.data.Store',
    storeId: 'usagePointDataValidationRulesSet',
    requires: ['Imt.validation.model.UsagePointDataValidationRulesSet'],
    model: 'Imt.validation.model.UsagePointDataValidationRulesSet',
    pageSize: 10
});
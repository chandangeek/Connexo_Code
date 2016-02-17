Ext.define('Imt.usagepointmanagement.store.UsagePointTypes', {
    extend: 'Ext.data.Store',
    model: 'Imt.usagepointmanagement.model.UsagePointType',
    data : [
        {id: 'UNMEASURED',    displayValue: Uni.I18n.translate('usagepointtypes.unmeasured', 'IMT', 'Unmeasured')},
        {id: 'SMART_DUMB', displayValue: Uni.I18n.translate('usagepointtypes.smartDumb', 'IMT', 'Smart dumb')},
        {id: 'INFRASTRUCTURE', displayValue: Uni.I18n.translate('usagepointtypes.infrastructure', 'IMT', 'Infrastructure')},
        {id: 'N_A', displayValue: Uni.I18n.translate('usagepointtypes.notAvailable', 'IMT', 'N/A')}
    ]
});
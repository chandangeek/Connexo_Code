Ext.define('Mdc.view.setup.devicedataestimation.RulePreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.deviceDataEstimationRulePreview',
    frame: true,
    requires: [
        'Mdc.model.EstimationRule'
    ],
    title: '',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    items: [
        {
            name: 'name',
            itemId: 'estimation-rule-field',
            fieldLabel: Uni.I18n.translate('estimationDevice.estimationRule', 'MDC', 'Estimation rule')
        },
        {
            name: 'displayName',
            itemId: 'estimator-field',
            fieldLabel: Uni.I18n.translate('estimationDevice.estimator', 'MDC', 'Estimator')
        },
        {
            name: 'active',
            fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
            itemId: 'status-field',
            renderer: function (value) {
                if (value) {
                    return Uni.I18n.translate('general.active', 'MDC', 'Active')
                } else {
                    return Uni.I18n.translate('general.inactive', 'MDC', 'Inactive')
                }
            }
        },
        {
            xtype: 'container',
            itemId: 'readingTypesArea',
            items: []
        },
        {
            xtype: 'property-form',
            itemId: 'rule-property-form',
            isEdit: false,
            defaults: {
                labelWidth: 250
            }
        }
    ],
    initComponent: function () {
        this.callParent(arguments);
    }
});
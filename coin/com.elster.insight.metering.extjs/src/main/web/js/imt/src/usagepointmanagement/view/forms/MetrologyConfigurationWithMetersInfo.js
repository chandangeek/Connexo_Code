Ext.define('Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMetersInfo', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metrology-configuration-with-meters-info',
    requires: [
        'Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsField',
        'Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsNoMetrologyGrid',
        'Imt.metrologyconfiguration.view.PurposesField'
    ],
    title: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
    ui: 'medium',
    style: 'padding-left: 0;padding-right: 0;',
    usagePoint: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'meter-activations-field',
                itemId: 'meter-activations-field',
                usagePoint: me.usagePoint,
                name: 'metrologyConfiguration.meterRoles',
                listeners: {
                    meterActivationsChange: function (allMetersSpecified) {
                        me.fireEvent('meterActivationsChange', allMetersSpecified);
                    }
                }
            },
            {
                xtype: 'purposes-field',
                itemId: 'purposes-field',
                name: 'metrologyConfiguration.purposes'
            }
        ];

        me.callParent(arguments);
    }
});
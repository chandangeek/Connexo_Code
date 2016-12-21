Ext.define('Imt.usagepointmanagement.view.forms.MetrologyConfigurationWithMetersInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-configuration-with-meters-info',
    requires: [
        'Imt.usagepointmanagement.view.forms.fields.MeterActivationsField',
        'Imt.metrologyconfiguration.view.PurposesField'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'meter-activations-field',
                itemId: 'meter-activations-field',
                hidden: true
            },
            {
                xtype: 'purposes-field',
                itemId: 'purposes-field'
            }
        ];

        me.callParent(arguments);
    }
});
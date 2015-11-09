Ext.define('Imt.usagepointmanagement.view.AssociatedMetrologyConfiguration', {
    extend: 'Ext.form.Panel',
    alias: 'widget.associated-metrology-configuration',
    itemId: 'associated-metrology-configuration',
    title: Uni.I18n.translate('usagePointManagement.linked-metrology-configuration', 'IMT', 'Associated Metrology configuration'),
    router: null,
    ui: 'tile',

    initComponent: function () {
        var me = this;
        me.items = [
            {
                labelAlign: 'right',
                xtype: 'fieldcontainer',
                labelWidth: 175,
                fieldLabel: Uni.I18n.translate('usagePointManagement.metrologyconfiguration', 'IMT', 'Metrology configuration'),
                layout: {
                    type: 'vbox'
                },
                itemId: 'associatedMetrologyConfiguration',
                items: []
            },

        ];
        me.callParent(arguments);
    }
});
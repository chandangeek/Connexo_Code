Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrologyConfigurationAttributesForm',
    itemId: 'metrologyConfigurationAttributesForm',
//    title: Uni.I18n.translate('usagePointManagement.attributes', 'IMT', 'Usage Point Attributes'),
//    router: null,
//    ui: 'tile',
    
//    requires: [
//        'Uni.form.field.Duration'
//    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'fieldcontainer',
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.generalAttributes.name', 'IMT', 'Name'),
                    },
                    {
                        name: 'version',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.generalAttributes.aliasName', 'IMT', 'Version'),
                    },
                    {
                        name: 'created',
                        itemId: 'fld-up-created',
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.generalAttributes.created', 'IMT', 'Created')
                    },
                    {
                        name: 'updated',
                        itemId: 'fld-up-updated',
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.generalAttributes.lastUpdate', 'IMT', 'Last update'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    }
                ]
            }
        ];
        me.callParent();
    }
});
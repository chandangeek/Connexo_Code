Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationAttributesForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrologyConfigurationAttributesForm',
    itemId: 'metrologyConfigurationAttributesForm',
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
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.generalAttributes.created', 'IMT', 'Created'),
                        renderer: function(value){
                            if(!Ext.isEmpty(value)) {
                                return Uni.DateTime.formatDateTimeLong(new Date(value));
                            }
                            return '-';
                        }
                    },
                    {
                        name: 'updated',
                        itemId: 'fld-up-updated',
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.generalAttributes.lastUpdate', 'IMT', 'Last update'),
                        renderer: function(value){
                            if(!Ext.isEmpty(value)) {
                                return Uni.DateTime.formatDateTimeLong(new Date(value));
                            }
                            return '-';
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        items: [
                            {
                                text: Uni.I18n.translate('general.cancel', 'IMT', 'Back'),
                                xtype: 'button',
                                ui: 'action',
                                itemId: 'backLink',
                                href: me.router.getRoute('metrologyconfiguration/overview').buildUrl(),
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent();
    }
});
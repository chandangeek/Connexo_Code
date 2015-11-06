Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview', {
    extend: 'Imt.metrologyconfiguration.view.GeneralPreview',
    alias: 'widget.metrologyConfigurationListPreview',
    itemId: 'metrologyConfigurationListPreview',
    record: null,

    requires: [
        'Imt.metrologyconfiguration.view.ActionMenu',
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'form',
                itemId: 'metrologyConfigurationListPreviewForm',
                layout: 'form',
                items: [
                    {
                        xtype:'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('metrologyconfiguration.general', 'IMT', 'General'),
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                                  {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('metrologyconfiguration.name', 'IMT', 'Name'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('metrologyconfiguration.version', 'IMT', 'Version'),
                                name: 'version'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('metrologyconfiguration.created', 'IMT', 'Created Date'),
                                name: 'created',
                                renderer: function(value){
                                    if(!Ext.isEmpty(value)) {
                                        return Uni.DateTime.formatDateTimeLong(new Date(value));
                                    }
                                    return '-';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('metrologyconfiguration.updated', 'IMT', 'Updated Date'),
                                name: 'updated',
                                renderer: function(value){
                                    if(!Ext.isEmpty(value)) {
                                        return Uni.DateTime.formatDateTimeLong(new Date(value));
                                    }
                                    return '-';
                                }
                            },
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});



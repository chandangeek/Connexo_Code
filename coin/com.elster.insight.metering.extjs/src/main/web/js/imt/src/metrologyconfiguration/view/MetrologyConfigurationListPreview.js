Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview', {
    extend: 'Imt.metrologyconfiguration.view.GeneralPreview',
    alias: 'widget.metrologyConfigurationListPreview',
    itemId: 'metrologyConfigurationListPreview',
    record: null,

    requires: [
        'Imt.metrologyconfiguration.view.ActionMenu'
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
                        labelAlign: 'top',
                        layout: 'vbox',
                        defaults: {
                            xtype: 'displayfield',
                            labelWidth: 200
                        },
                        items: [
                                  {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        name: 'name',
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.label.version', 'IMT', 'Version'),
                                name: 'version'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.label.createdDate', 'IMT', 'Created date'),
                                name: 'created',
                                renderer: function(value){
                                    if(!Ext.isEmpty(value)) {
                                        return Uni.DateTime.formatDateTimeLong(new Date(value));
                                    }
                                    return '-';
                                }
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.label.updated', 'IMT', 'Updated date'),
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

        me.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'IMT', 'Actions'),
                itemId: 'actionButton',
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'metrologyConfigurationActionMenu',
                    record: me.record
                }
            }
        ],
        me.callParent(arguments);
    }
});



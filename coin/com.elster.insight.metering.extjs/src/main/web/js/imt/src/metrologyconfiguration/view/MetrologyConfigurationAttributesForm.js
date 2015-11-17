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
                        fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                    },
                    {
                        name: 'version',
                        itemId: 'fld-up-version',
                        fieldLabel: Uni.I18n.translate('general.label.version', 'IMT', 'Version'),
                    },
                    {
                        name: 'created',
                        itemId: 'fld-up-created',
                        fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
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
                        fieldLabel: Uni.I18n.translate('general.label.lastUpdate', 'IMT', 'Last update'),
                        renderer: function(value){
                            if(!Ext.isEmpty(value)) {
                                return Uni.DateTime.formatDateTimeLong(new Date(value));
                            }
                            return '-';
                        }
                    }, 
                    {
               	 		fieldLabel: Uni.I18n.translate('general.label.lastUpdate', 'IMT', 'Validation rule sets'),
                        xtype: 'multiselect',                            
                        itemId: 'metrology-config-linked-val-rules-set',
                        name: 'linkedValidationRulesSets',
                        width: 500,
                        store: 'Imt.metrologyconfiguration.store.LinkedValidationRulesSet',
                        queryMode: 'local',
                        valueField: 'id',
                        displayField: 'name',
                        maxHeight: 100,
                        listConfig: { border: true }
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'no-validation-rulesets',
                        hidden: true,
                        value: '<div style="color: #FF0000">' + Uni.I18n.translate('validationTasks.general.noDeviceGroup', 'CFG', 'No validation rule ser assigned yet.') + '</div>',
                        htmlEncode: false,
                        labelwidth: 500,
                        width: 235
                    },
//                    {
//                        xtype: 'fieldcontainer',
//                        ui: 'actions',
//                        fieldLabel: '&nbsp',
//                        items: [
//                            {
//                                text: Uni.I18n.translate('general.button.cancel', 'IMT', 'Back'),
//                                xtype: 'button',
//                                ui: 'action',
//                                itemId: 'backLink',
//                                href: me.router.getRoute('administration/metrologyconfiguration').buildUrl(),
//                            }
//                        ]
//                    }
                ]
            }
        ];
        me.callParent();
    }
});
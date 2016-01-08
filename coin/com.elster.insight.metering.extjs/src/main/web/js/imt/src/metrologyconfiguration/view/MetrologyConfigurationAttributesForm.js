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
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.validationRuleSets', 'IMT', 'Validation rule sets'),
                        defaults: {
                            xtype: 'button',
                            ui: 'link',
                            href: '#/administration/metrologyconfiguration/' + me.mcid + '/associatedvalidationrulesets', 
                        },
                        items: [
                            {
                                name: 'validationRuleSetCount',
                                text: Uni.I18n.translatePlural('general.validationRuleSets', me.count, 'IMT',
                                        'No validation rule sets', '1 validation rule set', '{0} validation rule sets'),
                                itemId: 'validationRuleSetLink'
                            },
                        ]
                    }
                ]
            },
        ];
        me.callParent();
    }
});
Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.metrology-config-details-form',
    layout: 'column',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5,
        defaults: {
            xtype: 'displayfield',
            labelWidth: 200
        }
    },
    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                items: [
                    {
                        name: 'name',
                        itemId: 'fld-mc-name',
                        fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
                    },
                    {
                        name: 'description',
                        itemId: 'fld-mc-description',
                        fieldLabel: Uni.I18n.translate('general.label.description', 'IMT', 'Description')
                    },
                    {
                        name: 'status',
                        itemId: 'fld-mc-status',
                        fieldLabel: Uni.I18n.translate('general.label.status', 'IMT', 'Status'),
                        renderer: function (value) {
                            return value ? value.name : '';
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'area-mc-requirements',
                        fieldLabel: Uni.I18n.translate('general.label.usagePointRequirements', 'IMT', 'Usage point requirements'),
                        labelAlign: 'left'
                    },
                    {
                        name: 'serviceCategory',
                        itemId: 'fld-mc-service-category',
                        fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
                        renderer: function (value) {
                            return value ? value.name : '';
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'area-mc-meter-spec',
                        fieldLabel: Uni.I18n.translate('general.label.meterSpecifications', 'IMT', 'Meter specifications'),
                        labelAlign: 'left'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
                        name: 'meterRoles',
                        renderer: function (value) {
                            var result = '';

                            Ext.Array.each(value, function (role, index) {
                                if (index) {
                                    result += '<br>';
                                }
                                if (Ext.isObject(role)) {
                                    result += role.name;
                                }
                            });

                            return result || '-';
                        }
                    }
                ]
            },
            {
                items: [
                    {
                        name: 'purposes',
                        itemId: 'fld-mc-purposes',
                        fieldLabel: Uni.I18n.translate('general.label.purposes', 'IMT', 'Purposes'),
                        renderer: function (value) {
                            var result = '';

                            Ext.Array.each(value, function (role, index) {
                                //var url;

                                if (index) {
                                    result += '<br>';
                                }
                                if (Ext.isObject(role)) {
                                    //will be implemented later
                                    //url = me.router.getRoute().buildUrl();
                                    //result += '<a href="' + url + '"' + role.name + '</a>';
                                    result += role.name;
                                }
                            });

                            return result || '-';
                        }
                    }
                ]
            }
        ];

        me.callParent();
    }
});
Ext.define('Mdc.usagepointmanagement.view.UsagePointAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormMain',
    itemId: 'usagePointAttributesFormMain',
    requires: [
        'Uni.form.field.Duration'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                itemId: "usagePointGeneralAttributes",
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 150
                },
                items: [
                    {
                        name: 'mRID',
                        itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.mrid', 'MDC', 'MRID')
                    },
                    {
                        name: 'name',
                        itemId: 'fld-up-name',
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        renderer: function (value) {
                            return value ? value : '-';
                        }
                    },
                    {
                        name: 'serviceCategory',
                        itemId: 'fld-up-serviceCategory',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceCategory', 'MDC', 'Service category'),
                        renderer: function (value) {
                            var store = Ext.getStore('Mdc.usagepointmanagement.store.ServiceCategories'),
                                record = store.findRecord('name', value);
                            return record ? record.get('displayName') : value;
                        }
                    },
                    {
                        name: 'installationTime',
                        itemId: 'fld-up-created',
                        fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.created', 'MDC', 'Created'),
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                        }
                    },
                    {
                        name: 'location',
                        itemId: 'fld-device-location',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.location', 'MDC', 'Location'),
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
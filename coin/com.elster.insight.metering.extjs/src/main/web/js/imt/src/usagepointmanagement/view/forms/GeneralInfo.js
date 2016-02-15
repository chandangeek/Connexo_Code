Ext.define('Imt.usagepointmanagement.view.forms.GeneralInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.general-info-form',
    requires: [
        'Uni.form.field.DateTime'
    ],
    defaults: {
        labelWidth: 260,
        width: 595
    },
    items: [
        {
            xtype: 'textfield',
            name: 'mRID',
            itemId: 'up-mrid-textfield',
            fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
            required: true
        },
        {
            xtype: 'combobox',
            name: 'techInfoType',
            itemId: 'up-service-category-combo',
            fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
            required: true,
            store: 'Imt.servicecategories.store.ServiceCategories',
            displayField: 'displayName',
            valueField: 'name',
            queryMode: 'local',
            forceSelection: true
        },
        {
            xtype: 'textfield',
            name: 'name',
            itemId: 'up-name-textfield',
            fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
        },
        {
            xtype: 'date-time',
            name: 'createTime',
            itemId: 'up-createTime-textfield',
            fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
            required: true,
            layout: 'hbox',
            dateConfig: {
                flex: 1
            }
        },
        {
            xtype: 'textfield',
            name: 'location',
            itemId: 'up-location-combo',
            fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location')
        },
        {
            xtype: 'combobox',
            name: 'typeOfUsagePoint',
            itemId: 'up-typeOfUsagePoint-combo',
            fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point'),
            store: 'Imt.usagepointmanagement.store.UsagePointTypes',
            displayField: 'displayValue',
            valueField: 'id',
            queryMode: 'local',
            forceSelection: true,
            listeners: {
                change: {
                    fn: function (combo, newValue) {
                        if (!newValue) {
                            this.reset();
                        }
                    }
                }
            }
        },
        {
            xtype: 'textfield',
            name: 'readRoute',
            itemId: 'up-readRoute-textfield',
            fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route')
        },
        {
            xtype: 'textfield',
            name: 'serviceDeliveryRemark',
            itemId: 'up-serviceDeliveryRemark-textfield',
            fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark')
        }
    ]
});
Ext.define('Imt.usagepointmanagement.view.UsagePointSummary', {
    extend: 'Ext.form.Panel',
    requires: [
        'Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon'
    ],
    alias: 'widget.usage-point-summary',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 150
    },

    router: null,

    items: [
        {
            itemId: 'up-summary-mRID',
            name: 'mRID',
            fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID')
        },
        {
            xtype: 'displayfieldwithicon',
            itemId: 'up-summary-serviceCategory',
            name: 'serviceCategory',
            fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
        },
        {
            itemId: 'up-summary-name',
            name: 'name',
            fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
            renderer: function (value) {
                return value ? value : '-';
            }
        },
        {
            itemId: 'up-summary-created',
            name: 'installationTime',
            fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
            renderer: function (value) {
                return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
            }
        },
        {
            itemId: 'up-summary-location',
            name: 'location',
            fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location'),
            renderer: function (value) {
                return value ? value : '-';
            }
        },
        {
            itemId: 'up-summary-typeOfUsagePoint',
            name: 'typeOfUsagePoint',
            fieldLabel: Uni.I18n.translate('general.label.type', 'IMT', 'Type'),
            renderer: function (value) {
                var result = '-';

                if (!Ext.isEmpty(value)) {
                    result = Ext.getStore('Imt.usagepointmanagement.store.UsagePointTypes').findRecord('name', value).get('displayName');
                }

                return result;
            }
        },
        {
            xtype: 'displayfieldwithicon',
            itemId: 'up-summary-connectionState',
            name: 'connectionState',
            fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
        }
    ]
});
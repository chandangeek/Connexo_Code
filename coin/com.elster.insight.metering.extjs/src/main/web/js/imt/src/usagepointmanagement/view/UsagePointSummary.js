Ext.define('Imt.usagepointmanagement.view.UsagePointSummary', {
    extend: 'Ext.form.Panel',
    requires: [
        'Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon',
        'Imt.usagepointmanagement.view.forms.fields.UsagePointTypeDisplayField'
    ],
    alias: 'widget.usage-point-summary',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 120
    },

    router: null,

    items: [
        {
            xtype: 'displayfieldwithicon',
            itemId: 'up-summary-serviceCategory',
            name: 'serviceCategory',
            fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
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
            name: 'extendedGeoCoordinates',
            itemId: 'up-summary-geoCoordinates',
            fieldLabel: Uni.I18n.translate('general.label.coordinates', 'IMT', 'Coordinates'),
            renderer: function (value) {
                if (!Ext.isEmpty(value) && !Ext.isEmpty(value.coordinatesDisplay)) {
                    return Ext.String.htmlEncode(value.coordinatesDisplay);
                } else {
                    return '-'
                }
            }
        },
        {
            itemId: 'up-summary-location',
            name: 'extendedLocation',
            fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location'),
            renderer: function (value) {
                return value && value.formattedLocationValue ? Ext.String.htmlEncode(value.formattedLocationValue).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>') : '-';
            }
        },
        {
            xtype: 'usagepointtypedisplayfield',
            itemId: 'up-summary-typeOfUsagePoint',
            name: 'typeOfUsagePoint',
            fieldLabel: Uni.I18n.translate('general.label.type', 'IMT', 'Type')
        },
        {
            xtype: 'displayfieldwithicon',
            itemId: 'up-summary-connectionState',
            name: 'connectionState',
            fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
        }
    ],

    initComponent: function () {
        var me = this;

        me.bbar = [
            {
                itemId: 'up-summary-more-attributes-link',
                ui: 'link',
                text: Uni.I18n.translate('general.attributes.manage', 'IMT', 'Manage usage point attributes'),
                href: me.router.getRoute('usagepoints/view/attributes').buildUrl()
            }
        ];

        me.callParent(arguments);
    }
});
Ext.define('Mdc.usagepointmanagement.view.AddUsagePointForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.add-usage-point-form',
    requires: [
        'Uni.form.field.Duration',
        'Mdc.usagepointmanagement.view.InstallationTimeField'
    ],

    defaults: {
        labelWidth: 250
    },

    items: [
        {
            xtype: 'textfield',
            name: 'name',
            itemId: 'fld-up-name',
            required: true,
            width: 600,
            fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name')
        },
        {
            xtype: 'combobox',
            name: 'serviceCategory',
            displayField: 'displayName',
            valueField: 'name',
            store: 'Mdc.usagepointmanagement.store.ServiceCategories',
            itemId: 'fld-up-serviceCategory',
            required: true,
            editable: false,
            width: 600,
            fieldLabel: Uni.I18n.translate('usagePointManagement.generalAttributes.serviceCategory', 'MDC', 'Service category'),
        },
        {
            xtype: 'installationtimefield',
            dateFieldName: 'installationTime',
            itemId: 'up-createTime-installationtimefield',
            fieldLabel: Uni.I18n.translate('general.label.created', 'MDC', 'Created'),
            required: true
        },
        {
            xtype: 'coordinates',
            name: 'extendedGeoCoordinates',
            itemId: 'extendedGeoCoordinates',
            width: 600,
            fieldLabel: Uni.I18n.translate('general.coordinates', 'MDC', 'Coordinates')
        },
        {
            xtype: 'location',
            name: 'extendedLocation',
            itemId: 'extendedLocation',
            width: 610,
            findLocationsUrl: '/api/jsr/search/com.energyict.mdc.device.data.Device/locationsearchcriteria/location',
            locationDetailsUrl: '/api/ddr/devices/locations'
        },
        {
            xtype: 'fieldcontainer',
            ui: 'actions',
            fieldLabel: '&nbsp',
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                    xtype: 'button',
                    ui: 'action',
                    action: 'add',
                    itemId: 'usagePointAddButton'
                },
                {
                    text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                    xtype: 'button',
                    ui: 'link',
                    itemId: 'cancelLink',
                    href: '#/usagepoints/'
                }
            ]
        }
    ]
});
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
        labelWidth: 140
    },

    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
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
                itemId: 'up-summary-life-cycle',
                name: 'lifeCycle',
                fieldLabel: Uni.I18n.translate('general.usagePointLifeCycle', 'IMT', 'Usage point life cycle'),
                renderer: function (value) {
                    if (value) {
                        if (Imt.privileges.UsagePointLifeCycle.canView()) {
                            var url = me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle').buildUrl({usagePointLifeCycleId: value.id});
                            return '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>';
                        } else {
                            return Ext.String.htmlEncode(value.name);
                        }
                    } else {
                        return '-';
                    }
                }
            },
            {
                itemId: 'up-summary-state',
                name: 'state',
                fieldLabel: Uni.I18n.translate('general.state', 'IMT', 'State'),
                renderer: function (value) {
                    return value
                        ? Ext.String.htmlEncode(value.name) + ' (<a href="' + me.router.getRoute('usagepoints/view/history').buildUrl() + '">' +
                    Uni.I18n.translate('general.viewHistory', 'IMT', 'View history') + '</a>)'
                    + '<br><span style="font-size: 90%">'
                    + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(me.getRecord().get('lastTransitionTime')))], false)
                    + '</span>'
                        : '-';


                }
            },
            {
                xtype: 'displayfieldwithicon',
                itemId: 'up-summary-connectionState',
                name: 'connectionState',
                fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
            }
        ];

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
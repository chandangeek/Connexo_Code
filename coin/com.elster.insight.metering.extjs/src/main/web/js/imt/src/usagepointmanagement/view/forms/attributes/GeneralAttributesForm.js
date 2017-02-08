/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.attributes.GeneralAttributesForm', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    alias: 'widget.general-attributes-form',
    requires: [
        'Uni.form.field.Duration',
        'Imt.usagepointmanagement.view.forms.fields.DisplayFieldWithIcon',
        'Uni.form.field.Coordinates',
        'Uni.form.field.Location',
        'Imt.usagepointmanagement.view.forms.fields.UsagePointTypeDisplayField'
    ],
    router: null,

    initComponent: function () {
        var me = this;

        me.viewForm = [
            {
                name: 'mRID',
                itemId: 'fld-up-mRID',
                fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
                renderer: function (value) {
                    return value ? Ext.htmlEncode(value) : '-';
                }
            },
            {
                name: 'name',
                itemId: 'fld-up-name',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                renderer: function (value) {
                    return value ? Ext.htmlEncode(value) : '-';
                }
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'serviceCategory',
                itemId: 'fld-up-serviceCategory',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
            },
            {
                name: 'installationTime',
                itemId: 'fld-up-created',
                fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                name: 'extendedGeoCoordinates',
                itemId: 'fld-up-geoCoordinates',
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
                name: 'extendedLocation',
                itemId: 'fld-up-location',
                fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location'),
                renderer: function (value) {
                    return value && value.formattedLocationValue ? Ext.String.htmlEncode(value.formattedLocationValue).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>') : '-';
                }
            },
            {
                xtype: 'usagepointtypedisplayfield',
                name: 'typeOfUsagePoint',
                itemId: 'fld-up-typeOfUsagePoint',
                fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point')
            },
            {
                itemId: 'fld-up-life-cycle',
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
                itemId: 'fld-up-state',
                name: 'state',
                fieldLabel: Uni.I18n.translate('general.state', 'IMT', 'State'),
                renderer: function (value) {
                    return value
                        ? Ext.String.htmlEncode(value.name) + ' (<a href="' + me.router.getRoute('usagepoints/view/history').buildUrl() + '">' +
                    Uni.I18n.translate('general.viewHistory', 'IMT', 'View history') + '</a>)'
                    + '<br><span style="font-size: 90%">'
                    + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(me.record.get('lastTransitionTime')))], false)
                    + '</span>'
                        : '-';
                }
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'connectionState',
                itemId: 'fld-up-connectionState',
                fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
            },
            {
                name: 'readRoute',
                itemId: 'fld-up-readRoute',
                fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                name: 'serviceDeliveryRemark',
                itemId: 'fld-up-serviceDeliveryRemark',
                fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            }
        ];

        me.editForm = [
            {
                xtype: 'displayfield',
                name: 'mRID',
                itemId: 'fld-up-mRID',
                fieldLabel: Uni.I18n.translate('general.label.mRID', 'IMT', 'MRID'),
                renderer: function (value) {
                    return value ? Ext.htmlEncode(value) : '-';
                }
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'up-name-textfield',
                required: true,
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name')
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'serviceCategory',
                itemId: 'up-service-category-combo',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category')
            },
            {
                xtype: 'displayfield',
                name: 'installationTime',
                itemId: 'fld-up-created',
                fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(new Date(value)) : '-';
                }
            },
            {
                xtype: 'coordinates',
                name: 'extendedGeoCoordinates',
                itemId: 'fld-up-geoCoordinates',
                width: 421,
                displayResetButton: false,
                fieldLabel: Uni.I18n.translate('general.label.coordinates', 'IMT', 'Coordinates')
            },
            {
                xtype: 'location',
                name: 'extendedLocation',
                itemId: 'fld-location',
                width: 421,
                findLocationsUrl: '/api/jsr/search/com.elster.jupiter.metering.UsagePoint/locationsearchcriteria/location',
                locationDetailsUrl: '/api/udr/usagepoints/locations'
            },
            {
                xtype: 'usagepointtypedisplayfield',
                name: 'typeOfUsagePoint',
                itemId: 'fld-up-typeOfUsagePoint',
                fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point')
            },
            {
                xtype: 'displayfield',
                itemId: 'fld-up-life-cycle',
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
                xtype: 'displayfield',
                itemId: 'fld-up-state',
                name: 'state',
                fieldLabel: Uni.I18n.translate('general.state', 'IMT', 'State'),
                renderer: function (value) {
                    return value ? Ext.String.htmlEncode(value.name) + ' (<a href="' + me.router.getRoute('administration/usagepointlifecycles').buildUrl() + '">' +
                    Uni.I18n.translate('general.viewHistory', 'IMT', 'View history') + '</a>)' : '-';
                }
            },
            {
                xtype: 'displayfieldwithicon',
                name: 'connectionState',
                itemId: 'fld-up-connectionState',
                fieldLabel: Uni.I18n.translate('general.label.connectionState', 'IMT', 'Connection state')
            },
            {
                xtype: 'textfield',
                name: 'readRoute',
                itemId: 'fld-up-readRoute',
                fieldLabel: Uni.I18n.translate('general.label.readRoute', 'IMT', 'Read route')
            },
            {
                xtype: 'textfield',
                name: 'serviceDeliveryRemark',
                itemId: 'fld-up-serviceDeliveryRemark',
                fieldLabel: Uni.I18n.translate('general.label.serviceDeliveryRemark', 'IMT', 'Service delivery remark')
            }
        ];

        me.callParent();
    }
});
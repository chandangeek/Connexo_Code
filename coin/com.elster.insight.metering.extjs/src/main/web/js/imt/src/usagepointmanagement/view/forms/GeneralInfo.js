/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.GeneralInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.general-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormEmptyMessage',
        'Uni.form.field.DateTime',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.InstallationTimeField',
        'Uni.form.field.Coordinates',
        'Uni.form.field.Location'
    ],
    isPossibleAdd: true,
    defaults: {
        labelWidth: 260
    },

    layout: {
        type: 'vbox',
        align: 'stretchmax'
    },

    initComponent: function () {
        var me = this;

        me.items = [
            me.isPossibleAdd ?
            {
                itemId: 'general-info-warning',
                xtype: 'uni-form-error-message',
                hidden: true
            } :
            {
                itemId: 'not-possible-add',
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('usagepoint.add.notPossibleAdd', 'IMT', 'You cannot add usage point due to service category available')
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'up-name-textfield',
                fieldLabel: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                required: true
            },
            me.isPossibleAdd ?
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
                required: true,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'combobox',
                        name: 'serviceCategory',
                        itemId: 'up-service-category-combo',
                        store: 'Imt.usagepointmanagement.store.ServiceCategories',
                        displayField: 'displayName',
                        valueField: 'name',
                        queryMode: 'local',
                        forceSelection: true,
                        emptyText: Uni.I18n.translate('usagepoint.add.emptyText.serviceCategory', 'IMT', 'Select service category...'),
                        listeners: {
                            errorchange: {
                                fn: function (field, error) {
                                    if (field.rendered) {
                                        field.next().setVisible(Ext.isEmpty(error));
                                    }
                                }
                            }
                        }
                    },
                    {
                        xtype: 'displayfield',
                        width: 300,
                        margin: 0,
                        value: Uni.I18n.translate('usagepoint.add.clarification.serviceCategory', 'IMT', 'Service categories that you can\'t edit due to insufficient privileges are not included'),
                        fieldStyle: {
                            fontStyle: 'italic',
                            color: '#999'
                        }
                    }
                ]
            } :
            {
                xtype: 'displayfield',
                itemId: 'up-service-category-displayfield',
                fieldLabel: Uni.I18n.translate('general.label.serviceCategory', 'IMT', 'Service category'),
                required: true,
                htmlEncode: false,
                style: 'font-style: italic',
                value: '<span style="color: #686868; font-style: italic">'
                + Uni.I18n.translate('usagepoint.add.noServiceCategoryAvailable', 'IMT', 'No service category available due to insufficient privileges')
                + '</span>'
            },
            {
                xtype: 'techinfo-installationtimefield',
                dateFieldName: 'installationTime',
                itemId: 'up-createTime-installationtimefield',
                fieldLabel: Uni.I18n.translate('general.label.created', 'IMT', 'Created'),
                required: true
            },
            {
                xtype: 'coordinates',
                name: 'extendedGeoCoordinates',
                itemId: 'up-summary-geoCoordinates',
                fieldLabel: Uni.I18n.translate('general.label.coordinates', 'IMT', 'Coordinates'),
                width: 600
            },
            {
                xtype: 'location',
                itemId: 'up-summary-location',
                name: 'extendedLocation',
                width: 600,
                fieldLabel: Uni.I18n.translate('general.label.location', 'IMT', 'Location'),
                findLocationsUrl: '/api/jsr/search/com.elster.jupiter.metering.UsagePoint/locationsearchcriteria/location',
                locationDetailsUrl: '/api/udr/usagepoints/locations'
            },
            {
                xtype: 'combobox',
                name: 'typeOfUsagePoint',
                itemId: 'up-typeOfUsagePoint-combo',
                fieldLabel: Uni.I18n.translate('general.label.typeOfUsagePoint', 'IMT', 'Type of usage point'),
                store: 'Imt.usagepointmanagement.store.UsagePointTypes',
                displayField: 'displayName',
                valueField: 'name',
                queryMode: 'local',
                required: true,
                forceSelection: true,
                emptyText: Uni.I18n.translate('usagepoint.add.emptyText.typeOfUsagePoint', 'IMT', 'Select type of usage point...'),
                listeners: {
                    change: {
                        fn: function (field, newValue) {
                            if (Ext.isEmpty(newValue)) {
                                field.reset();
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
        ];

        me.callParent(arguments);
    }
});
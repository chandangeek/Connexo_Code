/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.UsagePointAttributesFormMain', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagePointAttributesFormMain',
    itemId: 'usagePointAttributesFormMain',
    title: Uni.I18n.translate('general.general.usagePointSummary', 'MDC', 'Usage point summary'),
    ui: 'tile',
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
                itemId: "usagePointGeneralAttributes",
                labelAlign: 'top',
                layout: 'vbox',
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 115
                },
                items: [
                    {
                        name: 'mRID',
                        itemId: 'fld-up-mRID',
                        fieldLabel: Uni.I18n.translate('usagePoint.generalAttributes.mrid', 'MDC', 'MRID')
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
                        name: 'extendedGeoCoordinates',
                        itemId: 'fld-device-coordinates',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.coordinates', 'MDC', 'Coordinates'),
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
                        itemId: 'fld-device-location',
                        fieldLabel: Uni.I18n.translate('deviceGeneralInformation.location', 'MDC', 'Location'),
                        renderer: function (value) {
                            if (!Ext.isEmpty(value) && !Ext.isEmpty(value.formattedLocationValue)) {
                                return Ext.String.htmlEncode(value.formattedLocationValue).replace(/(?:\\r\\n|\\r|\\n)/g, '<br>');
                            } else {
                                return '-'
                            }
                        }
                    }

                ]
            }
        ];
        me.callParent();
    }
});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.dataquality.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.imt-quality-preview',
    router: null,
    title: ' ',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'imt-quality-form',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    columnWidth: 0.5
                },
                items: [
                    {
                        defaults: {
                            xtype: 'fieldcontainer'
                        },
                        items: [
                            {
                                itemId: 'usagepoint-info-container',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                                        name: 'usagePointName',
                                        itemId: 'usagepoint-name-field',
                                        renderer: function (value) {
                                            if (value) {
                                                var url = me.router.getRoute('usagepoints/view').buildUrl({
                                                    usagePointId: encodeURIComponent(value)
                                                });
                                                return Imt.privileges.UsagePoint.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>' : value;
                                            }
                                        }
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.serviceCategory', 'IMT', 'Service category'),
                                        name: 'serviceCategory',
                                        itemId: 'service-category-field'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.metrologyConfiguration', 'IMT', 'Metrology configuration'),
                                        name: 'metrologyConfiguration',
                                        itemId: 'metrology-config-field',
                                        renderer: function (value) {
                                            var record = me.down('form').getRecord();

                                            if (record) {
                                                var url = me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({
                                                        mcid: value.id
                                                    }),
                                                    metrologyConfiguration = Imt.privileges.MetrologyConfig.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name),
                                                    icon = '<span class="icon-history" style="margin-left:10px;" data-qtip="' + Uni.I18n.translate('metrologyConfiguration.inThePast', 'IMT', 'Metrology configuration in the past') + '"></span>';

                                                return !!record.get('isEffectiveConfiguration') ? metrologyConfiguration : metrologyConfiguration + icon;
                                            }

                                        }
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.purpose', 'IMT', 'Purpose'),
                                        name: 'metrologyContract',
                                        itemId: 'purpose-field',
                                        renderer: function (value) {
                                            var record = me.down('form').getRecord();

                                            if (record) {
                                                var url = me.router.getRoute('usagepoints/view/purpose').buildUrl({
                                                    usagePointId: encodeURIComponent(record.get('usagePointName')),
                                                    purposeId: value.id
                                                });
                                                return Imt.privileges.UsagePoint.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name);
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.suspectReadings', 'IMT', 'Suspect readings'),
                                itemId: 'suspect-readings-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('general.registers', 'IMT', 'Registers'),
                                        name: 'registerSuspects',
                                        itemId: 'registers-field'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.channels', 'IMT', 'Channels'),
                                        name: 'channelSuspects',
                                        itemId: 'channels-field'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.lastSuspect', 'IMT', 'Last suspect'),
                                        name: 'lastSuspect',
                                        itemId: 'last-suspect-field',
                                        renderer: function (value) {
                                            return value ? Uni.DateTime.formatDateLong(new Date(value)) : '-';
                                        }
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.typeOfSuspects', 'IMT', 'Type of suspects'),
                                itemId: 'type-of-suspects-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                }
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'fieldcontainer'
                        },
                        items: [
                            {
                                margin: '0 0 0 150',
                                fieldLabel: Uni.I18n.translate('general.dataQuality', 'IMT', 'Data quality'),
                                itemId: 'data-quality-container',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250,
                                    fieldLabel: ''
                                },
                                items: [
                                    {
                                        name: 'amountOfSuspects',
                                        itemId: 'amount-of-suspects',
                                        renderer: function (value) {
                                            return '<span class="icon-flag5" style="color:red; margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.suspects', 'IMT', 'Suspects') + '"></span>' + value;
                                        }
                                    },
                                    {
                                        name: 'amountOfConfirmed',
                                        itemId: 'amount-of-confirmed',
                                        renderer: function (value) {
                                            return '<span class="icon-checkmark" style="margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.confirmed', 'IMT', 'Confirmed') + '"></span>' + value;
                                        }
                                    },
                                    {
                                        name: 'amountOfEstimates',
                                        itemId: 'amount-of-estimates',
                                        renderer: function (value) {
                                            return '<span class="icon-flag5" style="color:#33CC33; margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.estimates', 'IMT', 'Estimates') + '"></span>' + value;
                                        }
                                    },
                                    {
                                        name: 'amountOfInformatives',
                                        itemId: 'amount-of-informatives',
                                        renderer: function (value) {
                                            return '<span class="icon-flag5" style="color:#dedc49; margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.informatives', 'IMT', 'Informatives') + '"></span>' + value;
                                        }
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.editedValues', 'IMT', 'Edited values'),
                                itemId: 'edited-values-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('general.added', 'IMT', 'Added'),
                                        name: 'amountOfAdded',
                                        itemId: 'amount-of-added'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.edited', 'IMT', 'Edited'),
                                        name: 'amountOfEdited',
                                        itemId: 'amount-of-edited'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.removed', 'IMT', 'Removed'),
                                        name: 'amountOfRemoved',
                                        itemId: 'amount-of-removed'
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.typeOfEstimates', 'IMT', 'Type of estimates'),
                                itemId: 'type-of-estimates-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this;

        Ext.suspendLayouts();
        me.setTitle(record.get('usagePointName'));
        me.down('#imt-quality-form').loadRecord(record);
        updateContainer(me.down('#type-of-suspects-container'), record.suspectsPerValidator().getRange().map(prepareItem));
        updateContainer(me.down('#type-of-estimates-container'), record.estimatesPerEstimator().getRange().map(prepareItem));
        Ext.resumeLayouts(true);

        function updateContainer(container, items) {
            container.removeAll();
            container.add(items);
        }

        function prepareItem(item) {
            return {
                itemId: item.get('name'),
                fieldLabel: item.get('name'),
                value: item.get('value')
            }
        }
    }
});
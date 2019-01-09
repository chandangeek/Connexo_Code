/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.ZoneAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-zone-add',
    requires: [
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage',
        'Mdc.store.DeviceZonesTypes',
        'Mdc.store.DeviceZones',
        'Cfg.zones.store.Zones'
    ],
    device: null,


    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'device-zone-add-form',
                title: me.title,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        itemId: 'form-errors',
                        margin: '0 0 10 0',
                        maxWidth: 600,
                        hidden: true
                    },
                    {
                        xtype: 'panel',
                        //width: ' 100%',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'combobox',
                            labelWidth: 250,
                            maxWidth: 600,
                            allowBlank: false,
                            validateOnBlur: false,
                            required: true
                        },
                        items:[
                            {
                                itemId: 'zone-type',
                                xtype: 'combobox',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('devicezones.zoneType', 'MDC', 'Zone type'),
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                store: 'Mdc.store.DeviceZonesTypes',
                                disabled: me.edit,
                                emptyText: Uni.I18n.translate('devicezones.zoneTypeSelectorPrompt', 'MDC', 'Select a zone type...'),
                                listeners: {
                                    change: {
                                        fn: Ext.bind(me.onZoneTypeChange, me)
                                    }
                                }
                            },
                            {
                                itemId: 'zone-name',
                                xtype: 'combobox',
                                hidden: false,
                                name: 'zoneName',
                                fieldLabel: Uni.I18n.translate('general.zone', 'MDC', 'Zone'),
                                queryMode: 'local',
                                dataIndex: 'zones',
                                displayField: 'name',
                                valueField: 'id',
                                store: 'Cfg.zones.store.Zones',
                                emptyText: Uni.I18n.translate('devicezones.zoneSelectorPrompt', 'MDC', 'Select a zone...'),
                                disabled: me.edit
                            }
                        ]

                    },
                    {
                        itemId: 'device-zone-add-property-header',
                        margin: '16 0 0 0'
                    },
                    {
                        itemId: 'device-zone-add-property-form',
                        xtype: 'property-form',
                        margin: '16 0 0 0',
                        defaults: {
                            labelWidth: 250,
                            resetButtonHidden: false,
                            width: 336 // To be aligned with the above
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        labelWidth: 250,
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'mdc-zone-add-button',
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                ui: 'action',
                                action: 'add',
                                deviceId: me.device.get('name')
                            },
                            {
                                xtype: 'button',
                                itemId: 'mdc-zone-cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                action: 'cancel',
                                deviceId: me.device.get('name')
                            }
                        ]
                    }
                ]
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId:'deviceZones',
                        device: me.device
                    }
                ]
            }
        ];
        me.callParent(arguments);
    },

    onZoneTypeChange: function (combo, newValue) {
        var me = this,
            type = combo.findRecordByValue(newValue),
            zoneNameCombo = me.down('[name=zoneName]'),
            zoneNameStore = zoneNameCombo.getStore();

        if (type) {
            //Ext.suspendLayouts();
            zoneNameCombo.reset();
            //me.down('#rule-template-info').setInfoTooltip(null);
            //issueReasonCombo.reset();
            //me.down('property-form').loadRecord(Ext.create('Isu.model.CreationRuleTemplate'));
            //me.hideResetButtons();
            Ext.resumeLayouts(true);
            //me.setLoading();
            var zoneTypeIds = [];
            zoneTypeIds.push(combo.getValue());
            zoneNameStore.getProxy().setExtraParam('filter',
                Ext.encode([
                    {
                        property: 'zoneTypes',
                        value: zoneTypeIds
                    }
                ]));

            zoneNameStore.load({
                callback: function ()  {
                    if (zoneNameCombo.store.count() > 0)
                        me.down('#zone-name').show();
                    else
                        me.down('#zone-name').hide();
                }
            });

        }

    }
});




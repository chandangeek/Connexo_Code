/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicezones.ZoneEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-zone-edit',
    requires: [
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage',
        'Mdc.store.Zones'
    ],
    device: null,
    deviceZoneTypeId: null,


    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'device-zone-edit-form',
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
                        width: ' 100%',
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
                                name: 'zoneTypeName',
                                fieldLabel: Uni.I18n.translate('devicezones.zoneType', 'MDC', 'Zone type'),
                                queryMode: 'local',
                                displayField: 'zoneTypeName',
                                valueField: 'zoneTypeId',
                                store:   'Mdc.store.Zones',
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
                                name: 'zoneId',
                                fieldLabel: Uni.I18n.translate('general.zone', 'MDC', 'Zone'),
                                queryMode: 'local',
                                dataIndex: 'zones',
                                displayField: 'name',
                                valueField: 'id',
                                store:  'Mdc.store.Zones',
                                emptyText: Uni.I18n.translate('devicezones.zoneSelectorPrompt', 'MDC', 'Select a zone...'),
                                disabled: false,
                                editable: false
                            }
                        ]
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
                                itemId: 'mdc-zone-save-button',
                                text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                ui: 'action',
                                action: 'add',
                                deviceId: me.device.get('name'),
                                deviceZoneTypeId: me.deviceZoneTypeId
                            },
                            {
                                xtype: 'button',
                                itemId: 'mdc-zone-edit-cancel-button',
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
            zoneNameCombo = me.down('#zone-name'),
            zoneNameStore =  zoneNameCombo.getStore();

        zoneNameCombo.reset();
        Ext.resumeLayouts(true);
        var zoneTypeIds = [];
        zoneTypeIds.push(me.down('#mdc-zone-save-button').deviceZoneTypeId);
        zoneNameStore.getProxy().setExtraParam('filter',
            Ext.encode([
                {
                    property: 'zoneTypes',
                    value: zoneTypeIds
                }
            ]));

        zoneNameStore.load({
            success: function ()  {
                me.down('#zone-name]').bindStore(zoneNameStore);
            },
        });
    }
});




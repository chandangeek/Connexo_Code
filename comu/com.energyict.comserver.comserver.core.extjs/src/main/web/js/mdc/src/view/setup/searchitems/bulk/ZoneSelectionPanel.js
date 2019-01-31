/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.bulk.ZoneSelectionPanel', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.add-to-zone-panel',
    requires: [
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage',
        'Cfg.zones.store.ZoneTypes',
        'Mdc.store.Zones'
    ],
    device: null,
    disableAction: false,


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
                                itemId: 'zone-type-empty',
                                xtype: 'displayfield',
                                value: 'No zone type has been defined',
                                fieldLabel: Uni.I18n.translate('devicezones.zoneType', 'MDC', 'Zone type'),
                                //disabled: true,
                                hidden: !me.disableAction
                            },
                            {
                                itemId: 'zone-type',
                                xtype: 'combobox',
                                name: 'name',
                                fieldLabel: Uni.I18n.translate('devicezones.zoneType', 'MDC', 'Zone type'),
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                store: 'Cfg.zones.store.ZoneTypes',
                                disabled: me.edit,
                                emptyText: Uni.I18n.translate('devicezones.zoneTypeSelectorPrompt', 'MDC', 'Select a zone type...'),
                                listeners: {
                                    change: {
                                        fn: Ext.bind(me.onZoneTypeChange, me)
                                    }
                                },
                                hidden: me.disableAction
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
                                store: 'Mdc.store.Zones',
                                emptyText: Uni.I18n.translate('devicezones.zoneSelectorPrompt', 'MDC', 'Select a zone...'),
                                disabled: true,
                                hidden: me.disableAction
                            }
                        ]

                    },
                ]
            }
        ];
        me.callParent(arguments);
    },

    onZoneTypeChange: function (combo, newValue) {

        var me = this,
            type = combo.findRecordByValue(newValue),
            zoneNameCombo = me.down('#zone-name'),
            zoneNameStore = zoneNameCombo.getStore();

        if (type) {
            //Ext.suspendLayouts();
            zoneNameCombo.reset();
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
                callback: function (records, operation, success)  {
                    if (records.length > 0)
                        zoneNameCombo.setDisabled(false);
                    else
                        zoneNameCombo.setDisabled(true);
                }
            });

        }
    }
});

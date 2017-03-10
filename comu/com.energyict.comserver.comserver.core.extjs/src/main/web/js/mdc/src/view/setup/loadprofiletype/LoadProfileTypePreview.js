/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypePreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypePreview',
    itemId: 'loadProfileTypePreview',
    requires: [
        'Mdc.view.setup.loadprofiletype.LoadProfileTypeActionMenu',
        'Uni.form.field.ObisDisplay',
        'Mdc.store.Intervals',
        'Uni.form.field.CustomAttributeSetDisplay'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'load-profile-type-action-menu-button',
            menu: {
                xtype: 'load-profile-type-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'load-profile-type-preview-form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 140
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name','MDC','Name'),
                        name: 'name'
                    },
                    {
                        xtype: 'obis-displayfield',
                        name: 'obisCode'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                        name: 'timeDuration',
                        renderer: function (value) {
                            var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                            return intervalRecord ? Ext.String.htmlEncode(intervalRecord.get('name')) : '';
                        }
                    },
                    {
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.customattributeset', 'MDC', 'Custom attribute set'),
                        itemId: 'custom-attribute-set-displayfield-id',
                        xtype: 'custom-attribute-set-displayfield',
                        name: 'customPropertySet',
                        emptyText: '-'
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                itemId: 'registerTypesArea'
            }

        ]
    },

    updateRegisterTypes: function (selectedLoadProfileType) {
        Ext.suspendLayouts();
        this.down('#registerTypesArea').removeAll();
        for (var i = 0; i < selectedLoadProfileType.get('registerTypes').length; i++) {
            var readingType = selectedLoadProfileType.get('registerTypes')[i].readingType;

            this.down('#registerTypesArea').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: readingType
                }
            );
        }
        Ext.resumeLayouts(true);
    }
});



/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.zones.view.AddForm', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.zone-add',
    itemId: 'zone-add',
    returnLink: null,
    action: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                itemId: 'zones-add-form',
                title: me.title,
                ui: 'large',
                returnLink: me.returnLink,
                action: me.action,
                cancelLink: me.cancelLink,
                defaults: {
                    labelWidth: 250,
                    labelAlign: 'right',
                    width: 565
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'form-errors',
                        name: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'textfield',
                        itemId: 'zone-name',
                        name: 'name',
                        fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                        required: true,
                        allowBlank: false,
                    },
                    {
                        itemId: 'zone-type',
                        xtype: 'combobox',
                        name: 'zoneTypeName',
                        fieldLabel: Uni.I18n.translate('general.zoneType', 'CFG', 'Zone type'),
                        required: true,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'name',
                        store: 'Cfg.zones.store.ZoneTypes',
                        disabled: me.edit
                            ? true
                            : false
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'form-buttons',
                        fieldLabel: '&nbsp;',
                        layout: 'hbox',
                        margin: '20 0 0 0',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'btn-add-zone',
                                text: me.edit
                                    ? Uni.I18n.translate('general.save', 'CFG', 'Save')
                                    : Uni.I18n.translate('general.add', 'CFG', 'Add'),
                                ui: 'action',
                                action: me.edit
                                    ? 'editZone'
                                    : 'addZone'
                            },
                            {
                                xtype: 'button',
                                itemId: 'btn-cancel-add-zone',
                                text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                                ui: 'link',
                                action: 'cancel',
                                href: me.cancelLink
                            }
                        ]
                    }
                ]
            }

        ]
        me.callParent(arguments);
    }
});
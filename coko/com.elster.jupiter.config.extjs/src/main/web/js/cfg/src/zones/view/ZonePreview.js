Ext.define('Cfg.zones.view.ZonePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.zone-preview',
    purposeStore: null,
    requires: [
        'Cfg.privileges.Validation'
    ],


    title: Uni.I18n.translate('general.details','CFG','Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'zones-preview-actions-button',
            privileges: Cfg.privileges.Validation.adminZones,
            menu: {
                xtype: 'zones-action-menu',
                itemId: 'zones-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = [
                {
                xtype: 'form',
                border: false,
                itemId: 'zone-preview-form',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                layout: {
                                    type: 'vbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        dataIndex: 'name',
                                        fieldLabel: Uni.I18n.translate('general.name', 'CFG', 'Name'),
                                        itemId: 'zoneName',
                                        store: me.store,
                                        renderer: function (value) {
                                            return value;
                                        }
                                    },
                                    {
                                        xtype: 'displayfield',
                                        fieldLabel: Uni.I18n.translate('general.zoneType', 'CFG', 'Zone type'),
                                        dataIndex: 'zoneTypeName',
                                        itemId: 'zoneType',
                                        store: me.store,
                                        renderer: function (value) {
                                            return value;

                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent();
    }
});

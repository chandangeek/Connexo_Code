/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.metrologyconfiguration.UnlinkMeter', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.unlink-meter',
    itemId: 'unlinkMeter',
    requires: [
        'Uni.view.container.PreviewContainer',
        'Imt.usagepointmanagement.view.UsagePointSideMenu',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.form.ComboBoxWithEmptyComponent'
    ],
    router: null,
    usagePoint: null,
    meterName: null,
    meterRoleId: null,
    initComponent: function() {
       var me = this;
       me.content = [
           {
                xtype: 'form',
                title: Uni.I18n.translate('general.tooltip.unlinkx', 'IMT', "Unlink '{0}'", [me.meterName]),
                itemId: 'unlink-meter-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250,
                    width: 600,
                    enforceMaxLength: true
                },
                items: [
                    
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.unlinkDate', 'IMT', 'Unlink date'),
                        name: 'unlink-meter-date',
                        itemId: 'unlink-meter-date',
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'date-time',
                                itemId: 'unlink-date-on',
                                layout: 'hbox',
                                name: 'unlink-date-on',
                                dateConfig: {
                                    allowBlank: true,
                                    value: new Date(),
                                    editable: false,
                                    format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                },
                                hoursConfig: {
                                    fieldLabel: Uni.I18n.translate('general.lowercase.at', 'IMT', 'at'),
                                    labelWidth: 10,
                                    margin: '0 0 0 10',
                                    value: new Date().getHours()
                                },
                                minutesConfig: {
                                    width: 55,
                                    value: new Date().getMinutes()
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'unlink-meter-button',
                                text: Uni.I18n.translate('general.button.unlink', 'IMT', 'Unlink'),
                                ui: 'action',
                                meterName: me.meterName,
                                meterRoleId: me.meterRoleId,
                                usagePointName: me.usagePoint.get('name')
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.button.cancel', 'IMT', 'Cancel'),
                                ui: 'link',
                                href: me.router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl()
                            }
                        ]
                    }
                ]
           },
       ];
       me.side = [
        {
            ui: 'medium',
            items: [
                {
                    xtype: 'usage-point-management-side-menu',
                    itemId: 'usage-point-management-side-menu',
                    router: me.router,
                    usagePoint: me.usagePoint
                }
            ]
        }
    ];
       me.callParent(arguments);
    }
 });

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationPreview',
    itemId: 'loadProfileConfigurationPreview',
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu',
        'Mdc.model.LoadProfileConfiguration',
        'Uni.form.field.ObisDisplay',
        'Mdc.store.Intervals'
    ],
    title: '',
    frame: true,
    router: null,
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceType.admin,
            menu: {
                xtype: 'load-profile-configuration-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
                itemId: 'loadProfileConfigPreviewForm',
                layout: 'column',
                defaults: {
                xtype: 'fieldcontainer',
                    layout: 'form',
                    columnWidth: 0.5
            },
            items: [
                {
                    defaults: {
                        xtype: 'displayfield',
                        labelWidth: 200
                    },
                    items: [
                        {
                            fieldLabel: Uni.I18n.translate('general.loadProfileType', 'MDC', 'Load profile type'),
                            name: 'name',
                            renderer: function (value) {
                                var record = this.up('form').getRecord(),
                                    result = '';

                                if (me.router && record) {
                                    result = '<a href="'
                                        + me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').buildUrl(Ext.merge(me.router.arguments, {loadProfileConfigurationId: record.getId()}))
                                        + '">' + Ext.String.htmlEncode(value) + '</a>';
                                }

                                return result;
                            }
                        },
                        {
                            xtype: 'obis-displayfield',
                            name: 'overruledObisCode'
                        },
                        {
                            fieldLabel: Uni.I18n.translate('general.interval', 'MDC', 'Interval'),
                            name: 'timeDuration',
                            renderer: function (value) {
                                var intervalRecord = Ext.getStore('Mdc.store.Intervals').getById(value.id);
                                return intervalRecord ? Ext.String.htmlEncode(intervalRecord.get('name')) : '';
                            }
                        }
                    ]
                },
                {
                    xtype: 'displayfield',
                    name: 'channels',
                    itemId: 'channel-configurations-field',
                    fieldLabel: Uni.I18n.translate('general.channelConfigurations', 'MDC', 'Channel configurations'),
                    labelWidth: 200,
                    renderer: function (value) {
                        var typesString = '',
                            record = this.up('form').getRecord();

                        if (!Ext.isEmpty(value)) {
                            Ext.each(value, function (type) {
                                typesString += Ext.String.htmlEncode(type.name) + '<br />';
                            });
                        } else if (record && me.router) {
                            typesString = '<a href="'
                            + me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').buildUrl(Ext.merge(me.router.arguments, {loadProfileConfigurationId: record.getId()})) + '">'
                            + Uni.I18n.translatePlural('general.nrOfChannelConfigurations', 0, 'MDC', 'No channel configurations', '{0} channel configuration', '{0} channel configurations') + '</a>';
                        }
                        return typesString;
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
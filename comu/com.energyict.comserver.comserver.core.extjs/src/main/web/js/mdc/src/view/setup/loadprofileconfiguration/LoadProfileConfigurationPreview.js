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
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            privileges: Mdc.privileges.DeviceType.admin,
            iconCls: 'x-uni-action-iconD',
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
                            fieldLabel: 'Load profile type',
                            name: 'name',
                            renderer: function (value) {
                                var record = this.up('form').getRecord(),
                                    config = Ext.ComponentQuery.query('loadProfileConfigurationSetup')[0].config;

                                if (!Ext.isEmpty(record)) {
                                    return Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}/loadprofiles/{2}/channels">{3}</a>', config.deviceTypeId, config.deviceConfigurationId, record.getId(), Ext.String.htmlEncode(value));
                                }
                            }
                        },
                        {
                            xtype: 'obis-displayfield',
                            name: 'overruledObisCode'
                        },
                        {
                            fieldLabel: 'Interval',
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
                            record = this.up('form').getRecord(),
                            arguments;

                        if (!Ext.isEmpty(value)) {
                            Ext.each(value, function (type) {
                                typesString += Ext.String.htmlEncode(type.name) + '<br />';
                            });
                        } else if (record && me.router) {
                            arguments = Ext.clone(me.router.arguments);
                            arguments.loadProfileConfigurationId = record.getId();
                            typesString = '<a href="'
                            + me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view/loadprofiles/channels').buildUrl(arguments)
                            + '">0 ' + Uni.I18n.translate('general.channelconfigurations', 'MDC', 'channel configurations') + '</a>';
                        }
                        return typesString;
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
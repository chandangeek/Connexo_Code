Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationPreview',
    itemId: 'loadProfileConfigurationPreview',
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu',
        'Mdc.model.LoadProfileConfiguration'
    ],
    title: '',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'load-profile-configuration-action-menu'
            }
        }
    ],
    items: {
        xtype: 'form',
        itemId: 'loadProfileConfigPreviewForm',
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
                                return Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}/loadprofiles/{2}/channels">{3}</a>', config.deviceTypeId, config.deviceConfigurationId, record.getId(), value);
                            }
                        }
                    },
                    {
                        itemId: 'displayObis',
                        fieldLabel: 'OBIS code',
                        name: 'overruledObisCode'
                    },
                    {
                        fieldLabel: 'Interval',
                        name: 'timeDuration',
                        renderer: function (value) {
                            var intervalRecord = value ? Ext.getStore('Mdc.store.Intervals').getById(value.id) : null;

                            return intervalRecord ? intervalRecord.get('name') : '';
                        }
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                    labelWidth: 200
                },
                items: [
                    {
                        fieldLabel: 'Channel configurations',
                        name: 'channels',
                        renderer: function (value) {
                            var typesString = '';
                            if (!Ext.isEmpty(value)) {
                                Ext.each(value, function (type) {
                                    typesString += type.name + '<br />';
                                });
                            }
                            return typesString;
                        }
                    }
                ]
            }
        ]
    }
});
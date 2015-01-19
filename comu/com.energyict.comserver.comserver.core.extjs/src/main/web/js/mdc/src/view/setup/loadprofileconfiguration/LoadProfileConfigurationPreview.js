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
    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.deviceType'),
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
                                return Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}/loadprofiles/{2}/channels">{3}</a>', config.deviceTypeId, config.deviceConfigurationId, record.getId(), value);
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
                            return intervalRecord ? intervalRecord.get('name') : '';
                        }
                    }
                ]
            },
            {
                itemId: 'channel-configurations-field',
                fieldLabel: Uni.I18n.translate('general.channelConfigurations', 'MDC', 'Channel configurations'),
                labelWidth: 200,
                labelStyle : 'padding:3px 14px 0px 0px;',
                items: [
                    {
                        xtype: 'displayfield',
                        name: 'channels',
                        renderer: function (value) {
                            var typesString = '',
                                emptyBtn = this.up('#channel-configurations-field').down('button');

                            if (!Ext.isEmpty(value)) {
                                if (emptyBtn) { emptyBtn.hide(); }
                                this.show();
                                Ext.each(value, function (type) {
                                    typesString += type.name + '<br />';
                                });
                            } else {
                                this.hide();
                                var linkButton = Ext.widget('button', {
                                    text: '0 ' + Uni.I18n.translate('general.channelconfigurations', 'MDC', 'channel configurations'),
                                    ui: 'link',
                                    hrefTarget: '_self'
                                });
                                if (!emptyBtn) {
                                    this.up('#channel-configurations-field').add(linkButton);
                                }
                                return;
                            }
                            return typesString;
                        }
                    }
                ]
            }
        ]
    }
});
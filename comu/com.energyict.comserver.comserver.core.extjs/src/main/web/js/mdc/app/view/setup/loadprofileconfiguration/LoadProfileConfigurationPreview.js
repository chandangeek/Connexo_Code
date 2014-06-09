Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileConfigurationPreview',
    itemId: 'loadProfileConfigurationPreview',
    requires: [
        'Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationActionMenu'
    ],
    height: 310,
    frame: true,
    intervalStore: null,
    editActionName: null,
    deleteActionName: null,

    items: [
        {
            xtype: 'form',
            name: 'loadProfileConfigurationDetails',
            itemId: 'loadProfileConfigurationDetails',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Load profile type: ',
                            name: 'name',
                            renderer: function (value) {
                                var record = this.up('#loadProfileConfigurationDetails').getRecord();
                                if (!Ext.isEmpty(record)) {
                                    return Ext.String.format('<a href="#/administration/devicetypes/{0}/deviceconfigurations/{1}/loadprofiles/{2}/channels">{3}</a>', this.up('#loadProfileConfigurationPreview').deviceTypeId, this.up('#loadProfileConfigurationPreview').deviceConfigurationId, record.getData().id, value);
                                }
                            }
                        },
                        {
                            xtype: 'displayfield',
                            itemId: 'displayObis',
                            fieldLabel: 'OBIS code: ',
                            name: 'overruledObisCode'
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Interval: ',
                            name: 'timeDuration',
                            renderer: function (value) {
                                var intervalRecord = this.up('#loadProfileConfigurationPreview').intervalStore.findRecord('id', value.id);
                                if (!Ext.isEmpty(intervalRecord)) {
                                    return intervalRecord.getData().name;
                                }
                            }
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Channels: ',
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
    ],

    initComponent: function () {
        this.tools = [
            {
                xtype: 'button',
                text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
                iconCls: 'x-uni-action-iconD',
                menu: {
                    xtype: 'load-profile-configuration-action-menu'
                }
            }
        ];
        this.callParent(arguments);
    }

});
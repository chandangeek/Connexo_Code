Ext.define('Mdc.view.setup.devicetype.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.deviceTypeSideMenu',
    title: Uni.I18n.translate('general.deviceType', 'MDC', 'Device type'),
    deviceTypeId: null,
    isDataLoggerSlave: undefined,
    initComponent: function () {
        var me = this;

        me.menuItems = [
            {
                itemId: 'overviewLink',
                href: '#/administration/devicetypes/' + me.deviceTypeId
            },
            {
                title: Uni.I18n.translate('devicetypemenu.datasources', 'MDC', 'Data sources'),
                items: [
                    {
                        text: Uni.I18n.translate('general.loadProfileTypes', 'MDC', 'Load profile types'),
                        itemId: 'loadProfilesLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/loadprofiles'
                    },
                    {
                        text: Uni.I18n.translate('general.logbookTypes', 'MDC', 'Logbook types'),
                        itemId: 'logbooksLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/logbooktypes'
                    },
                    {
                        text: Uni.I18n.translate('general.registerTypes', 'MDC', 'Register types'),
                        itemId: 'registerConfigsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/registertypes'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                        itemId: 'configurationsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/deviceconfigurations'
                    },
                    {
                        text: Uni.I18n.translate('devicetypemenu.conflictingMappings', 'MDC', 'Conflicting mappings'),
                        itemId: 'conflictingMappingLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/conflictmappings'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicetypemenu.customattributes', 'MDC', 'Custom attributes'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetypemenu.customattribute.sets', 'MDC', 'Custom attribute sets'),
                        itemId: 'configurationsLink',
                        href: '#/administration/devicetypes/' + me.deviceTypeId + '/customattributesets'
                    }
                ]
            }
        ];

        me.executeIfDataLoggerSlave(function() {
            me.down('#logbooksLink').hide();
        });

        me.callParent(arguments);
    },

    executeIfDataLoggerSlave: function(executeWhenDetermined) {
        var me = this;
        if (me.isDataLoggerSlave === undefined) {
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
                success: function (deviceType) {
                    me.isDataLoggerSlave = deviceType.isDataLoggerSlave();
                    if (me.isDataLoggerSlave) {
                        executeWhenDetermined();
                    }
                }
            });
        } else if (me.isDataLoggerSlave) {
            executeWhenDetermined();
        }
    },

    executeIfNoDataLoggerSlave: function(executeWhenDetermined) {
        var me = this;
        if (me.isDataLoggerSlave === undefined) {
            Ext.ModelManager.getModel('Mdc.model.DeviceType').load(me.deviceTypeId, {
                success: function (deviceType) {
                    me.isDataLoggerSlave = deviceType.isDataLoggerSlave();
                    if (!me.isDataLoggerSlave) {
                        executeWhenDetermined();
                    }
                }
            });
        } else if (!me.isDataLoggerSlave) {
            executeWhenDetermined();
        }
    }
});


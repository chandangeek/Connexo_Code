Ext.define('Mdc.view.setup.devicetype.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.deviceTypeSideMenu',
    title: Uni.I18n.translate('devicetype.sidemenu.title', 'MDC', 'Device type'),
    deviceTypeId: null,
    initComponent: function () {
        this.menuItems = [
            {
                itemId: 'overviewLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId
            },
            {
                title: Uni.I18n.translate('devicetypemenu.datasources', 'MDC', 'Data sources'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetypemenu.loadProfiles', 'MDC', 'Load profile types'),
                        itemId: 'loadProfilesLink',
                        href: '#/administration/devicetypes/' + this.deviceTypeId + '/loadprofiles'
                    },
                    {
                        text: Uni.I18n.translate('devicetype.sidemenu.logbookType', 'MDC', 'Logbook types'),
                        itemId: 'logbooksLink',
                        href: '#/administration/devicetypes/' + this.deviceTypeId + '/logbooktypes'
                    },
                    {
                        text: Uni.I18n.translate('devicetypemenu.registerTypes', 'MDC', 'Register types'),
                        itemId: 'registerConfigsLink',
                        href: '#/administration/devicetypes/' + this.deviceTypeId + '/registertypes'
                    }
                ]
            },
            {
                title: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                items: [
                    {
                        text: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                        itemId: 'configurationsLink',
                        href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations'
                    }
                ]
            }
        ];
        this.callParent(this);
    }
});


Ext.define('Mdc.view.setup.devicetype.DeviceTypeMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceTypeMenu',
    deviceTypeId: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: Uni.I18n.translate('devicetypemenu.overview', 'MDC', 'Overview'),
                pressed: false,
                itemId: 'deviceTypeOverviewLink',
                href: '#setup/devicetypes/' + this.deviceTypeId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicetypemenu.registerTypes', 'MDC', 'Register types'),
                pressed: false,
                itemId: 'registerConfigsLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/registertypes',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicetypemenu.loadProfiles', 'MDC', 'Load profiles'),
                pressed: false,
                itemId: 'loadProfilesLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicetypemenu.logbooks', 'MDC', 'Logbooks'),
                pressed: false,
                itemId: 'logbooksLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/logbooks',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                pressed: false,
                itemId: 'configurationsLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});


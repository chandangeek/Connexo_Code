Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceConfigurationMenu',
    deviceTypeId: null,
    deviceConfigurationId: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.overview', 'MDC', 'Overview'),
                pressed: false,
                itemId: 'deviceConfigurationOverviewLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('deviceconfigurationmenu.registerTypes', 'MDC', 'Register types'),
                pressed: false,
                itemId: 'registerConfigsLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/registerconfigurations',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('deviceconfigurationmenu.loadProfiles', 'MDC', 'Load profiles'),
                pressed: false,
                itemId: 'loadProfilesLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('deviceconfigurationmenu.logbooks', 'MDC', 'Logbooks'),
                pressed: false,
                itemId: 'logbooksLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId+ '/logbooks',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});

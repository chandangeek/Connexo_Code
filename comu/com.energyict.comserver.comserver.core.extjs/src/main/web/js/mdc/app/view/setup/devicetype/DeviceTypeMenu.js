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
                href: '#/administration/devicetypes/' + this.deviceTypeId,
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicetypemenu.registerTypes', 'MDC', 'Register types'),
                pressed: false,
                itemId: 'registerConfigsLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/registertypes',
                hrefTarget: '_self'
            }, {
                text: Uni.I18n.translate('devicetypemenu.loadProfiles', 'MDC', 'Load profiles'),
                pressed: false,
                itemId: 'loadProfilesLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: 'Logbook types',
                pressed: false,
                itemId: 'logbooksLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/logbooktypes',
                hrefTarget: '_self'
            },
            {
                text: Uni.I18n.translate('devicetypemenu.configurations', 'MDC', 'Configurations'),
                pressed: false,
                itemId: 'configurationsLink',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});


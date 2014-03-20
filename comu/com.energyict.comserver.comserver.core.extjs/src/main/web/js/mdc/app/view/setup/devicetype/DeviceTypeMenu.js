Ext.define('Mdc.view.setup.devicetype.DeviceTypeMenu', {
    extend: 'Uni.view.navigation.SubMenu',
    alias: 'widget.deviceTypeMenu',
    deviceTypeId: null,
    toggle: null,
    initComponent: function () {
        this.callParent(this);
        this.add(
            {
                text: 'Overview',
                pressed: false,
                itemId: 'deviceTypeOverviewLink',
                href: '#setup/devicetypes/' + this.deviceTypeId,
                hrefTarget: '_self'
            },
            {
                text: 'Register types',
                pressed: false,
                itemId: 'registerConfigsLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/registertypes',
                hrefTarget: '_self'
            }, {
                text: 'Load profiles',
                pressed: false,
                itemId: 'loadProfilesLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/loadprofiles',
                hrefTarget: '_self'
            }, {
                text: 'Logbooks',
                pressed: false,
                itemId: 'logbooksLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/logbooks',
                hrefTarget: '_self'
            },
            {
                text: 'Configurations',
                pressed: false,
                itemId: 'configurationsLink',
                href: '#setup/devicetypes/' + this.deviceTypeId + '/deviceconfigurations',
                hrefTarget: '_self'
            }
        );
        this.toggleMenuItem(this.toggle);
    }
});


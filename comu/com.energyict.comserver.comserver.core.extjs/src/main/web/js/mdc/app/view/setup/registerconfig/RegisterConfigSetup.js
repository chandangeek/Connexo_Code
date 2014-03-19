Ext.define('Mdc.view.setup.registerconfig.RegisterConfigSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerConfigSetup',
    autoScroll: true,
    itemId: 'registerConfigSetup',
    deviceTypeId: null,
    deviceConfigId: null,
    requires: [
        'Mdc.view.setup.registerconfig.RegisterConfigGrid',
        'Mdc.view.setup.registerconfig.RegisterConfigFilter',
        'Mdc.view.setup.registerconfig.RegisterConfigPreview',
        'Uni.view.navigation.SubMenu',
        'Uni.view.breadcrumb.Trail'
    ],
    content: [
        {
            xtype: 'container',
            cls: 'content-container',
            itemId: 'stepsContainer',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: Uni.I18n.translate('registerConfig.deviceConfiguration', 'MDC', 'Device configuration'),
                    margins: '10 10 0 20'
                },
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerConfig.registerConfigs', 'MDC', 'Register configurations') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerConfigTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerConfigGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerConfigPreviewContainer'

                }
            ]}
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'stepsMenu'
        },
        {
            xtype: 'registerConfigFilter',
            name: 'filter'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerConfigGridContainer').add(
            {
                xtype: 'registerConfigGrid',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );
        this.down('#registerConfigPreviewContainer').add(
            {
                xtype: 'registerConfigPreview',
                deviceTypeId: this.deviceTypeId,
                deviceConfigId: this.deviceConfigId
            }
        );

        this.initStepsMenu();
    },

    initStepsMenu: function () {
        var me = this;
        var stepsMenu = this.getStepsMenuCmp();

        var deviceConfigButton = stepsMenu.add({
            text: 'Overview',
            pressed: false,
            itemId: 'deviceConfigOverviewLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId,
            hrefTarget: '_self'
        });

        var registerTypesButton = stepsMenu.add({
            text: 'Register configurations',
            pressed: true,
            itemId: 'registerConfigsLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/registerconfigurations',
            hrefTarget: '_self'
        });

        var loadProfilesButton = stepsMenu.add({
            text: 'Load profiles',
            pressed: false,
            itemId: 'loadProfilesLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/loadprofiles',
            hrefTarget: '_self'
        });

        var logbooksButton = stepsMenu.add({
            text: 'Logbooks',
            pressed: false,
            itemId: 'logbooksLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/logbooks',
            hrefTarget: '_self'
        });

        var operationsButton = stepsMenu.add({
            text: 'Operations',
            pressed: false,
            itemId: 'operationsLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/operations',
            hrefTarget: '_self'
        });

        var communicationButton = stepsMenu.add({
            text: 'Communication',
            pressed: false,
            itemId: 'communicationLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/communication',
            hrefTarget: '_self'
        });

        var firmwareButton = stepsMenu.add({
            text: 'Firmware',
            pressed: false,
            itemId: 'firmwareLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/firmware',
            hrefTarget: '_self'
        });

        var securityButton = stepsMenu.add({
            text: 'Security',
            pressed: false,
            itemId: 'securityLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations/' + me.deviceConfigId + '/security',
            hrefTarget: '_self'
        });

    },

    getStepsMenuCmp: function () {
        return this.down('#stepsMenu');
    }

});



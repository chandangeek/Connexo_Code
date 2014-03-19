Ext.define('Mdc.view.setup.register.RegisterMappingsSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerMappingsSetup',
    autoScroll: true,
    itemId: 'registerMappingSetup',
    deviceTypeId: null,
    requires: [
        'Mdc.view.setup.register.RegisterMappingsGrid',
        'Mdc.view.setup.register.RegisterMappingsFilter',
        'Mdc.view.setup.register.RegisterMappingPreview',
        'Uni.view.navigation.SubMenu',
        'Uni.view.breadcrumb.Trail'
    ],
    /* layout: {
     type: 'vbox',
     align: 'stretch'
     },*/
    // cls: 'content-container',
//    border: 0,
//    region: 'center',

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
                /* {
                 xtype: 'breadcrumbTrail',
                 region: 'north',
                 padding: 6
                 },*/
                {
                    xtype: 'component',
                    html: Uni.I18n.translate('registerMapping.deviceType', 'MDC', 'Device type'),
                    margins: '10 10 0 20'
                },
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerMapping.registerTypes', 'MDC', 'Register types') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerTypeTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingGridContainer'
                },
                {
                    xtype: 'component',
                    height: 25
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingPreviewContainer'

                }
            ]}
    ],

    side: [
        {
            xtype: 'navigationSubMenu',
            itemId: 'stepsMenu'
        },
        {
            xtype: 'registerMappingFilter',
            name: 'filter'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerMappingGridContainer').add(
            {
                xtype: 'registerMappingsGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
        this.down('#registerMappingPreviewContainer').add(
            {
                xtype: 'registerMappingPreview',
                deviceTypeId: this.deviceTypeId
            }
        );

        this.initStepsMenu();
    },

    initStepsMenu: function () {
        var me = this;
        var stepsMenu = this.getStepsMenuCmp();

        var deviceTypeButton = stepsMenu.add({
            text: 'Overview',
            pressed: false,
            itemId: 'deviceTypeOverviewLink',
            href: '#setup/devicetypes/' + me.deviceTypeId,
            hrefTarget: '_self'
        });

        var registerTypesButton = stepsMenu.add({
            text: 'Register types',
            pressed: true,
            itemId: 'registerTypesLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/registertypes',
            hrefTarget: '_self'
        });

        var loadProfilesButton = stepsMenu.add({
            text: 'Load profiles',
            pressed: false,
            itemId: 'loadProfilesOverviewLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/loadprofiles',
            hrefTarget: '_self'
        });

        var logBooksButton = stepsMenu.add({
            text: 'Logbooks',
            pressed: false,
            itemId: 'logbooksOverviewLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/logbooks',
            hrefTarget: '_self'
        });

        var configurationsButton = stepsMenu.add({
            text: 'Device configurations',
            pressed: false,
            itemId: 'deviceConfigurationsOverviewLink',
            href: '#setup/devicetypes/' + me.deviceTypeId + '/deviceconfigurations',
            hrefTarget: '_self'
        });

    },

    getStepsMenuCmp: function () {
        return this.down('#stepsMenu');
    },

    getStepsContainerCmp: function () {
        return this.down('#stepsContainer');
    }

});



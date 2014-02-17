Ext.define('Mdc.view.setup.register.RegisterMappingAdd', {
    extend: 'Ext.container.Container',
    alias: 'widget.registerMappingAdd',
    autoScroll: true,
    requires: [
        'Mdc.view.setup.register.RegisterMappingAddGrid'
    ],
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    deviceTypeId: null,
    cls: 'content-wrapper',
    items: [
        {
            xtype: 'container',
            cls: 'content-container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'breadcrumbTrail',
                    region: 'north',
                    padding: 6
                },
                {
                    xtype: 'component',
                    html: I18n.translate('registerMapping.deviceType', 'MDC', 'Device type'),
                    margins: '10 10 0 20'
                },
                {
                    xtype: 'component',
                    html: '<h1>' + I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types') + '</h1>',
                    margins: '10 10 10 10',
                    itemId: 'registerTypeAddTitle'
                },
                {
                    xtype: 'container',
                    items: [],
                    itemId: 'registerMappingAddGridContainer'
                },
                {
                    xtype: 'fieldcontainer',
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    margins: '10 10 0 20',
                    items: [
                        {
                            text: I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            action: 'addRegisterMappingAction',
                            itemId: 'addButton'
                        },
                        {
                            xtype: 'component',
                            padding: '3 0 0 10',
                            itemId: 'cancelLink',
                            autoEl: {
                                tag: 'a',
                                href: '#setup/devicetypes/' + this.deviceTypeId + '/registertypes',
                                html: I18n.translate('general.cancel', 'MDC', 'Cancel')
                            }
                        }
                    ]
                }
            ]}
    ],


    initComponent: function () {
        this.callParent(arguments);
        this.down('#registerMappingAddGridContainer').add(
            {
                xtype: 'registerMappingAddGrid',
                deviceTypeId: this.deviceTypeId
            }
        );
        this.down('#cancelLink').autoEl.href = '#setup/devicetypes/' + this.deviceTypeId + '/registertypes';
    }
});



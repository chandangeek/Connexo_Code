Ext.define('Mdc.view.setup.register.RegisterMappingAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.registerMappingAdd',

    requires: [
        'Mdc.view.setup.register.RegisterMappingAddGrid',
        'Mdc.view.setup.register.RegisterMappingsAddFilter'
    ],

    deviceTypeId: null,

    content: [
        {
            xtype: 'container',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'component',
                    html: '<h1>' + Uni.I18n.translate('registerMappingAdd.addRegisterTypes', 'MDC', 'Add register types') + '</h1>',
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
                            text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                            xtype: 'button',
                            action: 'addRegisterMappingAction',
                            itemId: 'addButton'
                        },
                        {
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            xtype: 'button',
                            ui: 'link',
                            itemId: 'cancelLink',
                            href: '#setup/devicetypes/' + this.deviceTypeId + '/registertypes',
                        }
                    ]
                }
            ]}
    ],

    /* side: [
     {
     xtype: 'registerMappingAddFilter',
     name: 'filter'
     }
     ],*/

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



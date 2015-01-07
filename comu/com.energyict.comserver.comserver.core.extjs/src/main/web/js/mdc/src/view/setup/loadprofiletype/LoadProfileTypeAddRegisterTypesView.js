Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeAddRegisterTypesView',
    itemId: 'loadProfileTypeAddRegisterTypesView',

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesView.title', 'MDC', 'Add register types'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'loadProfileTypeAddRegisterTypesGrid'
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});


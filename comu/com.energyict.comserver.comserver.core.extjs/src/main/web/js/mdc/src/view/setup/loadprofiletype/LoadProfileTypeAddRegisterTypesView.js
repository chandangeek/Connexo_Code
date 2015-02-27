Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeAddRegisterTypesView',
    itemId: 'loadProfileTypeAddRegisterTypesView',
    title: Uni.I18n.translate('setup.loadprofiletype.LoadProfileTypeAddRegisterTypesView.title', 'MDC', 'Add register types'),
    ui: 'large',

    content: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: {
                xtype: 'loadProfileTypeAddRegisterTypesGrid'
            }
        }
    ]
});
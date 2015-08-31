Ext.define('Mdc.view.setup.register.RegisterMappingsFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registerMappingFilter',
    title: Uni.I18n.translate('general.filter','MDC','Filter'),
    cls: 'filter-form',

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'name',
                    fieldLabel: 'Name'
                }
            ]
        }
    ],

    buttons: [
        {
            text: Uni.I18n.translate('general.apply','MDC','Apply'),
            action: 'filter'
        },
        {
            text: Uni.I18n.translate('general.reset','MDC','Reset'),
            action: 'reset'
        }
    ],


    initComponent: function () {
        this.callParent(arguments);
    }
});
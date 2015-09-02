Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeSideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.loadProfileTypeSideFilter',
    title: Uni.I18n.translate('general.filter','MDC','Filter'),
    width: 200,
    ui: 'filter',

    items: [
        {
            xtype: 'filter-form',
            items: [
                {
                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            fieldLabel: Uni.I18n.translate('general.name','MDC','Name'),
                            labelAlign: 'top'
                        },
                        {
                            xtype: 'textfield',
                            name: 'obis',
                            fieldLabel: 'OBIS code',
                            labelAlign: 'top'
                        }
                    ]
                }
            ]
        }
    ],

    buttons: [
        {
            text: Uni.I18n.translate('general.apply','MDC','Apply'),
            action: 'applyloadprofiletypefilter'
        },
        {
            text: Uni.I18n.translate('general.reset','MDC','Reset'),
            action: 'reset'
        }
    ]
});
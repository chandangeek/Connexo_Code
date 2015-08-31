Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSideFilter', {
    extend: 'Ext.form.Panel',
    alias: 'widget.securitySettingSideFilter',
    title: 'Filter',
    ui: 'filter',
    width: 180,

    requires: [
        'Uni.component.filter.view.Filter'
    ],

    items: [
        {
            xtype: 'filter-form',
            items: [
                {
                    xtype: 'textfield',
                    name: 'LoadProfileType',
                    fieldLabel: 'Load profile type',
                    labelAlign: 'top'
                },
                {
                    xtype: 'textfield',
                    name: 'obis',
                    fieldLabel: 'OBIS code',
                    labelAlign: 'top'
                },
                {
                    xtype: 'button',
                    ui: 'link',
                    text: Uni.I18n.translate('general.moreCriteria','MDC','More criteria'),
                    aling: 'left'
                }
            ]
        }
    ],

    buttons: [
        {
            text: Uni.I18n.translate('general.apply','MDC','Apply'),
            action: 'applysecurityfilter'
        },
        {
            text: Uni.I18n.translate('general.reset','MDC','Reset'),
            action: 'reset'
        }
    ]
});
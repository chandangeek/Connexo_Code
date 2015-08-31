Ext.define('Mdc.view.setup.securitysettings.SecuritySettingSorting', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.securitySettingSorting',
    title: 'Sort',
        name: 'sortitemspanel',
        height: 40,
        emptyText: 'None',
        tools: [
            {
                xtype: 'button',
                action: 'addSort',
                text: Uni.I18n.translate('general.addSort','MDC','Add sort'),
                menu: {
                    name: 'addsortitemmenu'
                }
            }
        ]
});
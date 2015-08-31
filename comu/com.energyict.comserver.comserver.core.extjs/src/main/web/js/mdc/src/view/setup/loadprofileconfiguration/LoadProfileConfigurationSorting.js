Ext.define('Mdc.view.setup.loadprofileconfiguration.LoadProfileConfigurationSorting', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.loadProfileConfigurationSorting',
    title: 'Sort',
    name: 'sortitemspanel',
    height: 40,
    emptyText: Uni.I18n.translate('general.none','MDC','None'),
    tools: [
        {
            xtype: 'button',
            action: 'addSort',
            text: Uni.I18n.translate('loadprofileconfiguration.addSort','MDC','Add sort'),
            menu: {
                name: 'addsortitemmenu'
            }
        }
    ]
});
Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeFiltering', {
    extend: 'Uni.view.panel.FilterToolbar',
    alias: 'widget.loadProfileTypeFiltering',
    itemId: 'LoadProfileTypeFiltering',
    title: Uni.I18n.translate('general.filters','MDC','Filters'),
    emptyText: Uni.I18n.translate('general.none','MDC','None'),
    height: 40
});

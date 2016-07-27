Ext.define('Imt.purpose.view.NoReadingsFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-readings-found-panel',
    title: Uni.I18n.translate('readings.list.empty', 'IMT', 'No data is available'),
    reasons: [
        Uni.I18n.translate('readings.list.reason1', 'IMT', 'No data has been collected yet'),
        Uni.I18n.translate('readings.list.reason2', 'IMT', 'Filter is too narrow')
    ]
});
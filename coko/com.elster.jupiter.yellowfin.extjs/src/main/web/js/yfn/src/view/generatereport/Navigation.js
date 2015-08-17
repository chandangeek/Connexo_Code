Ext.define('Yfn.view.generatereport.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.generatereport-navigation',
    width: 256,
    jumpForward: false,
    jumpBack: true,
    ui: 'medium',
    padding: '0 0 0 0',
    margin: '0 0 0 0',
    title: Uni.I18n.translate('generatereport.wizardMenu', 'YFN', 'Generate report'),
    items: [
        {
            itemId: 'selectReportType',
            text: Uni.I18n.translate('generatereport.selectReportType', 'YFN', 'Select report type')
        },
        {
            itemId: 'selectReportPrompts',
            text: Uni.I18n.translate('generatereport.selectReportFilters', 'YFN', 'Select report filters')
        },
        /*{
            itemId: 'selectReportFilters',
            text: Uni.I18n.translate('generatereport.selectReportFilters', 'YFN', 'Select report filters')
        },*/
        {
            itemId: 'reportGenerator',
            text: Uni.I18n.translate('generatereport.reportGenerator', 'YFN', 'Report generator')
        }
    ]
});

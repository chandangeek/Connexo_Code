Ext.define('Isu.view.issues.DetailNavigation', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.issue-detail-navigation',
    defaultButtonUI: 'link',
    rtl: false,
    items: [
        {
            xtype: 'tbfill'
        },
        {
            itemId: 'data-collection-issue-navigation-previous',
            text: Uni.I18n.translate('general.previous', 'ISU', 'Previous'),
            action: 'prev'
        },
        {
            itemId: 'data-collection-issue-navigation-next',
            text: Uni.I18n.translate('general.next', 'ISU', 'Next'),
            action: 'next'
        }
    ]
});
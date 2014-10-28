Ext.define('Isu.view.issues.GroupingTitle', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issues-grouping-title',
    ui: 'medium',
    title: Uni.I18n.translate('general.issuesFor', 'ISU', 'Issues for {0}: {1}'),
    padding: 0
});
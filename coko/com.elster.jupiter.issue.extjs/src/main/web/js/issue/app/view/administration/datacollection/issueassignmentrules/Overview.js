Ext.define('Isu.view.administration.datacollection.issueassignmentrules.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Isu.view.administration.datacollection.issueassignmentrules.List',
        'Uni.view.container.PreviewContainer'
    ],
    alias: 'widget.issue-assignment-rules-overview',

    content: {
        itemId: 'title',
        xtype: 'panel',
        ui: 'large',
        title: Uni.I18n.translate('issue.administration.assignment', 'ISU', 'Issue assignment rules'),
        items: {
            itemId: 'issues-rules-list',
            xtype: 'preview-container',
            grid: {
                xtype: 'issues-assignment-rules-list'
            },
            emptyComponent: {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'left'
                },
                minHeight: 20,
                items: [
                    {
                        xtype: 'image',
                        margin: '0 10 0 0',
                        src: '../ext/packages/uni-theme-skyline/build/resources/images/shared/icon-info-small.png',
                        height: 20,
                        width: 20
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'component',
                                html: '<b>' + Uni.I18n.translate('issueAssignment.empty.title', 'ISU', 'No issue assignment rules found') + '</b><br>' +
                                    Uni.I18n.translate('issueAssignment.empty.detail', 'ISU', 'There are no issue assignment rules. This could be because:') + '<lv><li>&nbsp&nbsp' +
                                    Uni.I18n.translate('issueAssignment.empty.list.item', 'ISU', 'No issue assignment rules have been defined yet.')
                            }
                        ]
                    }
                ]
            },
            previewComponent: null
        }
    }
});
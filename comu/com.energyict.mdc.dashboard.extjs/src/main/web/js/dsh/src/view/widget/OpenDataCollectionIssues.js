Ext.define('Dsh.view.widget.OpenDataCollectionIssues', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.open-data-collection-issues',
    buttonAlign: 'right',
    router: null,

    tbar: {
        xtype: 'container',
        itemId: 'connection-summary-title-panel'
    },

    setRecord: function (record) {
        var me = this,
            grid = me.down('#open-data-collection-issues-grid'),
            countContainer = me.down('#open-data-collection-issues-count-container'),
            dockedLinksContainer = me.down('#open-data-collection-issues-docked-links'),
            titleContainer = me.down('#connection-summary-title-panel'),
            assigned = record.getAssignedToMeIssues(),
            unassigned = record.getUnassignedIssues(),
            issuesCount;

        grid.reconfigure(assigned.topMyIssues());
        issuesCount = grid.getStore().getCount();
        countContainer.removeAll();
        dockedLinksContainer.removeAll();
        titleContainer.removeAll();

        if (issuesCount) {
            countContainer.add({
                xtype: 'container',
                html: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.topIssues', 'DSH', 'Top {0} most urgent issues assigned to me'), issuesCount )
            });
        }

        var assignedFilter = Ext.apply(assigned.get('filter'), {
            assignee: assigned.get('filter').assigneeId + ':' + assigned.get('filter').assigneeType,
            status: 'status.open'
        });

        var unassignedFilter = {
            assignee: unassigned.get('filter').assigneeId + ':' + unassigned.get('filter').assigneeType,
            status: 'status.open'
        };

        titleContainer.add({
            xtype: 'container',
            html: '<h3>' + Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.header', 'DSH', 'Open data collection issues ({0})'), assigned.get('total') ) + '</h3>'
        });

        dockedLinksContainer.add([
            {
                xtype: 'button',
                text: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.assignedToMe', 'DSH', 'Assigned to me ({0})'), assigned.get('total')),
                ui: 'link',
                href: me.router.getRoute('workspace/datacollection/issues').buildUrl(null, assignedFilter)
            },
            {
                xtype: 'button',
                text: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.unassigned', 'DSH', 'Unassigned ({0})'), unassigned.get('total')),
                ui: 'link',
                href: me.router.getRoute('workspace/datacollection/issues').buildUrl(null, unassignedFilter)
            }
        ]);
    },

    initComponent: function() {
        var me = this;

        me.items = [
            {
                xtype: 'container',
                margin: '5 0 0 0',
                itemId: 'open-data-collection-issues-count-container'
            },
            {
                xtype: 'gridpanel',
                emptyText: Uni.I18n.translate('overview.widget.openDataCollectionIssues.empty.title', 'MDC', 'No issues assigned to me found'),
                margin: '5 0 10 0',
                itemId: 'open-data-collection-issues-grid',
                hideHeaders: true,
                padding: 0,
                columns: [
                    {

                        dataIndex: 'dueDate',
                        align: 'right',
                        width: 40,
                        renderer: function(value, meta, record){
                            if (value) {
                                if ( moment().isAfter(moment(value))) {
                                    meta['tdAttr'] = 'data-qtip="' + Uni.I18n.translate('overview.widget.openDataCollectionIssues.overdue', 'DSH', 'Overdue') + '"';
                                    return '<img src="/apps/dsh/resources/images/widget/blocked.png" />';
                                } else {
                                    if(moment().endOf('day').isAfter(moment(value))) {
                                        meta['tdAttr'] = 'data-qtip="' + Uni.I18n.translate('overview.widget.openDataCollectionIssues.dueToday', 'DSH', 'Due today') + '"';
                                        return '<img src="/apps/dsh/resources/images/widget/blocked.png" />';
                                    } else {
                                        if (moment().add(1, 'day').endOf('day').isAfter(moment(value)) ) {
                                            meta['tdAttr'] = 'data-qtip="' + Uni.I18n.translate('overview.widget.openDataCollectionIssues.dueTomorrow', 'DSH', 'Due tomorrow') + '"';
                                            return '<img src="/apps/dsh/resources/images/widget/inactive.png" />';
                                        }
                                    }
                                }
                            }
                        }
                    },
                    {
                        dataIndex: 'title',
                        flex: 1,
                        renderer: function(value, meta, record){
                            var href = me.router.getRoute('workspace/datacollection/issues/view').buildUrl({id: record.get('id')});
                            return '<a href="' + href + '">' + value + '</a>'
                        }
                    }
                ]
            },
            {
                xtype: 'container',
                itemId: 'open-data-collection-issues-docked-links'
            }
        ];

        this.callParent(arguments);
    }

});

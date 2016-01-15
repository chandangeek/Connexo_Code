Ext.define('Dsh.view.widget.OpenDataCollectionIssues', {
    extend: 'Ext.panel.Panel',
    ui: 'tile',
    alias: 'widget.open-data-collection-issues',
    buttonAlign: 'left',
    layout: 'fit',
    router: null,
    header: {
        ui: 'small'
    },

    setRecord: function (record) {
        var me = this;
        if(me.rendered) {
            var elm = me.down('#issues-dataview'),
                countContainer = me.down('#open-data-collection-issues-count-container'),
                dockedLinksContainer = me.down('#open-data-collection-issues-docked-links'),
                assigned = record.getAssignedToMeIssues(),
                unassigned = record.getUnassignedIssues(),
                store = assigned.topMyIssues(),
                issuesCount = store.getCount(),
                title = '<h3>' + Uni.I18n.translate(
                    'overview.widget.openDataCollectionIssues.header',
                    'DSH',
                    'Open data collection issues ({0})',
                    assigned.get('total')
                ) + '</h3>';

            me.setTitle(title);
            store.each(function (item) {
                var dueDate = item.get('dueDate');
                item.set('href', me.router.getRoute('workspace/datacollectionissues/view').buildUrl({issueId: item.get('id')}));

                if (dueDate) {
                    if (moment().isAfter(moment(dueDate))) {
                        item.set('tooltip', Uni.I18n.translate('overview.widget.openDataCollectionIssues.overdue', 'DSH', 'Overdue'));
                        item.set('icon', '/apps/dsh/resources/images/widget/blocked.png');
                    } else {
                        if (moment().endOf('day').isAfter(moment(dueDate))) {
                            item.set('tooltip', Uni.I18n.translate('overview.widget.openDataCollectionIssues.dueToday', 'DSH', 'Due today'));
                            item.set('icon', '/apps/dsh/resources/images/widget/blocked.png');
                        } else {
                            if (moment().add(1, 'day').endOf('day').isAfter(moment(dueDate))) {
                                item.set('tooltip', Uni.I18n.translate('overview.widget.openDataCollectionIssues.dueTomorrow', 'DSH', 'Due tomorrow'));
                                item.set('icon', '/apps/dsh/resources/images/widget/inactive.png');
                            }
                        }
                    }
                }
            });

            elm.bindStore(store);

            Ext.suspendLayouts();

            countContainer.removeAll();
            dockedLinksContainer.removeAll();

            if (issuesCount === 0) {
                countContainer.add({
                    xtype: 'label',
                    text: Uni.I18n.translate('operator.dashboard.issuesEmptyMsg', 'DSH', 'No open issues assigned to me')
                });
            }
            if (issuesCount) {
                countContainer.add({
                    xtype: 'container',
                    html: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.topIssues', 'DSH', 'Top {0} most urgent issues assigned to me'), issuesCount)
                });
            }

            var assignedFilter = {
                assignee: assigned.get('filter').assigneeId + ':' + assigned.get('filter').assigneeType,
                status: ['status.open', 'status.in.progress'],
                groupingType: 'none',
                sort: ['dueDate', 'modTime']
            };

            var unassignedFilter = {
                assignee: unassigned.get('filter').assigneeId + ':' + unassigned.get('filter').assigneeType,
                status: 'status.open',
                groupingType: 'none',
                sort: ['dueDate', 'modTime']
            };

            dockedLinksContainer.add([
                {
                    xtype: 'button',
                    itemId: 'lnk-assigned-issues-link',
                    text: Ext.String.format(Uni.I18n.translate('overview.widget.openDataCollectionIssues.assignedToMe', 'DSH', 'Assigned to me ({0})'), assigned.get('total')),
                    ui: 'link',
                    href: me.router.getRoute('workspace/datacollectionissues').buildUrl(null, assignedFilter)
                },
                {
                    xtype: 'button',
                    itemId: 'lnk-unassigned-issues-link',
                    text: Ext.String.format(Uni.I18n.translate('general.unassignedCounter', 'DSH', 'Unassigned ({0})'), unassigned.get('total')),
                    ui: 'link',
                    href: me.router.getRoute('workspace/datacollectionissues').buildUrl(null, unassignedFilter)
                }
            ]);
            Ext.resumeLayouts(true);
        }
    },

    tbar: {
        xtype: 'container',
        itemId: 'open-data-collection-issues-count-container'
    },

    items: [
        {
            xtype: 'dataview',
            itemId: 'issues-dataview',
            itemSelector: 'a.x-btn.flag-toggle',

            tpl: new Ext.XTemplate(
                '<table style="margin: 5px 0 10px 0; table-layout: fixed; width: 100%;">',
                '<tpl for=".">',
                '<tr id="{id}" class="issue">',
                '<td height="26" width="20" data-qtip="{tooltip}"><img style="margin: 5px 5px 0 0" src="{icon}" /></td>',
                '<td data-qtip="{title}" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"><a href="{href}">{title}</a></td>',
                '</tr>',
                '</tpl>',
                '</table>'
            )
        }
    ],

    bbar: {
        xtype: 'container',
        itemId: 'open-data-collection-issues-docked-links'
    }
});

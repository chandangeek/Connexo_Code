Ext.define('Idv.view.NonEstimatedDataGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'no-estimated-data-grid',
    title: Uni.I18n.translate('issues.NonEstimatedDataGrid.title', 'IDV', 'Non estimated data'),
    ui: 'medium',
    requires: [
        'Ext.grid.feature.GroupingSummary',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    features: [{
        id: 'group',
        ftype: 'groupingsummary',
        hideGroupedHeader: true,
        enableGroupingMenu: false,
        startCollapsed: true,
        groupHeaderTpl: '<span style="display: inline; margin: 0px 10px 0px 0px">{[values.children[0].data.readingType.fullAliasName]}</span>' + //
        '<span class="uni-icon-info-small" style="cursor: pointer; display: inline-block; width: 16px; height: 16px; float: none" data-qtip="' + Uni.I18n.translate('readingType.tooltip', 'UNI', 'Reading type info') + '"></span>', //{rows.length}
    }],

    listeners: {
        groupclick: function (view, node, group, e, eOpts) {
            if (e.target.getAttribute('class') == 'uni-icon-info-small') {
                var g = view.features[0];
                g.isExpanded(group) ? g.collapse(group) : g.expand(group);
                var widget = Ext.widget('reading-type-displayfield');
                var readingType = this.store.getGroups(group).children[0].get('readingType');
                widget.handler(readingType, readingType.fullAliasName);
            }

            return false;
        }
    },

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                text: Uni.I18n.translate('issues.NonEstimatedDataGrid.dataSource', 'IDV', 'Data source'),
                renderer: function (value, meta, record) {
                    if (record.get('startTime') == record.get('endTime')) {
                        return Uni.DateTime.formatDateTimeShort(new Date(record.get('startTime')));
                    } else {
                        return  Uni.DateTime.formatDateTimeShort(new Date(record.get('startTime')))
                            + ' - '
                            + Uni.DateTime.formatDateTimeShort(new Date(record.get('endTime')))
                    }
                },
                summaryType: 'count',
                summaryRenderer: function(value, summaryData, dataIndex) {
                    return Uni.I18n.translatePlural('issues.NonEstimatedDataGrid.total', value, 'IDV', 'Total of {0} blocks');
                },
                flex: 1
            },
            {
                text: Uni.I18n.translate('issues.NonEstimatedDataGrid.amountOfSuspects', 'IDV', 'Amount of suspects'),
                dataIndex: 'amountOfSuspects',
                summaryType: 'sum',
                width: 200
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                //privileges: !Isu.privileges.Issue.adminDevice,
                menu: {
                    xtype: 'menu',
                    defaultAlign: 'tr-br?',
                    plain: true,
                    items: {
                        text: Uni.I18n.translate('issues.actionMenu.viewData', 'IDV', 'View data'),
                        action: 'viewData',
                        hrefTarget: '_blank'
                    }
                }
            }
        ];

        this.callParent(arguments);
    }
});
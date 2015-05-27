Ext.define('Idv.view.NonEstimatedDataGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'no-estimated-data-grid',
    title: 'Non Estimated Data', //todo: translate
    ui: 'medium',
    requires: [
        'Ext.grid.feature.Grouping',
        'Uni.form.field.ReadingTypeDisplay'
    ],

    features: [{
        ftype: 'grouping',
        groupHeaderTpl: '{[values.children[0].data.readingType.fullAliasName]}' + //<span style="display: block; float: left; margin: 0px 10px 0px 0px"></span>
        '<span class="uni-icon-info-small" style="cursor: pointer; display: inline-block; width: 16px; height: 16px; float: right;" data-qtip="' + Uni.I18n.translate('readingType.tooltip', 'UNI', 'Reading type info') + '"></span>', //{rows.length}

    }],

    listeners: {
        groupclick: function (view, node, group, e, eOpts) {
            if (e.target.getAttribute('class') == 'uni-icon-info-small') {
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
            {text: 'startTime', dataIndex: 'startTime', flex: 1},
            {text: 'endTime', dataIndex: 'endTime', flex: 1},
            {text: 'amountOfSuspects', dataIndex: 'amountOfSuspects'},
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                //privileges: !Isu.privileges.Issue.adminDevice,
                menu: {
                    xtype: 'menu',
                    items: {
                        text: Uni.I18n.translate('issues.actionMenu.viewData', 'ISU', 'View data'),
                        action: 'viewData',
                        hrefTarget: '_blank'
                    }
                }
            }
        ];

        this.callParent(arguments);
    }
});
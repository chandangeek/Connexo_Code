Ext.define('Isu.view.component.AssigneeCombo', {
    extend: 'Uni.form.field.GridPicker',

    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.IssueAssignees',
    displayField: 'name',
    valueField: 'idx',

    triggerAction: 'query',
    queryMode: 'remote',
    queryParam: 'like',
    allQuery: '',
    lastQuery: '',
    queryDelay: 100,
    minChars: 0,
    disableKeyFilter: true,
    queryCaching: false,

    formBind: true,
    typeAhead: true,

    anchor: '100%',

    forceSelection: true,

    gridConfig: {
        emptyText: Uni.I18n.translate('issues.noAssigneeFound','ISU','No assignee found'),
        resizable: false,
        stripeRows: true,

        features: [
            {
                ftype: 'grouping',
                groupHeaderTpl: '{name}',
                collapsible: false
            }
        ],
        columns: [
            {
                header: false,
                xtype: 'templatecolumn',
                tpl: '<tpl if="type"><span class="isu-icon-{type} isu-assignee-type-icon"></span></tpl> {name}',
                flex: 1
            }
        ]
    },
    listeners: {
        focus: {
            fn: function(combo){
                if (!combo.getValue()) {
                    combo.doQuery(combo.getValue());
                }
            }
        },
        change: {
            fn: function(combo, newValue){
                if (!newValue){
                    combo.reset();
                }
            }
        },
        beforequery: {
            fn: function(queryPlan) {
                var store = queryPlan.combo.store;
                if (queryPlan.query) {
                    store.group('type');
                } else {
                    store.clearGrouping();
                }
            }
        }
    }
});


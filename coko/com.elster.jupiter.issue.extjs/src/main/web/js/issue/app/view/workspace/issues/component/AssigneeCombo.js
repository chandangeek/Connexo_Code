Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.ux.Rixo.form.field.GridPicker',

    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.Assignee',
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
        emptyText: 'No assignee found',
        resizable: false,
        stripeRows: true,

        features: [
            {
                ftype: 'grouping',
                groupHeaderTpl: '<span class="isu-icon-{name}"></span> {name}',
                collapsible: false
            }
        ],
        columns: [
            {
                header: false,
                xtype: 'templatecolumn',
                tpl: "<tpl if='id &gt; "empty" '>Child</tpl>",
                flex: 1
            }
           /* {
                dataIndex: 'name', flex: 1
            }*/
        ]
    }
});


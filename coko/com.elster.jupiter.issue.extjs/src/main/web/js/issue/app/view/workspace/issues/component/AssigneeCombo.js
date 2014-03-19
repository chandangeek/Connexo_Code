Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.ux.Rixo.form.field.GridPicker',

    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.Assignee',
    displayField: 'name',
    valueField: 'id',

    queryMode: 'remote',
    queryParam: 'like',
    queryDelay: 100,
    minChars: 2,

    groupField: 'type',
    groupDisplayField: 'type',
    formBind: true,

    typeAhead: false,
    hideTrigger: true,
    anchor: '100%',
    emptyText: 'type something',

    gridConfig: {
        features: [
            {
                ftype: 'grouping',
                groupHeaderTpl: '{name}',
                collapsible: false
            }
        ],
        columns: [
//            {
//                xtype: 'rownumberer'
//            },
            {
                dataIndex: 'name', flex: 1
            }
        ]
    }
});


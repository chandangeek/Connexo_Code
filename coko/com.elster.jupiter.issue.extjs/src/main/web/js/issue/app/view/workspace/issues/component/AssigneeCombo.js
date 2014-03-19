Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.ux.Rixo.form.field.GridPicker',

    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.Assignee',
    displayField: 'name',
    valueField: 'id',

    queryMode: 'remote',
    queryParam: 'like',
    queryDelay: 100,
    minChars: 1,

    formBind: true,

    typeAhead: true,
//    hideTrigger: true,
    anchor: '100%',
    emptyText: 'type something',

    forceSelection: true,

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


Ext.define('Isu.view.workspace.issues.component.DepartmentCombo', {
    extend: 'Ext.ux.Rixo.form.field.GridPicker',

    alias: 'widget.issues-departmenr-combo',
    store: 'Isu.store.UserGroupList',
    displayField: 'name',
    valueField: 'id',

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
        emptyText: 'No department found',
        resizable: false,
        stripeRows: true,

        columns: [
            {
                header: false,
                xtype: 'templatecolumn',
                tpl: '<span class="isu-icon-GROUP isu-assignee-type-icon"></span> {name}',
                flex: 1
            }
        ]
    }
});


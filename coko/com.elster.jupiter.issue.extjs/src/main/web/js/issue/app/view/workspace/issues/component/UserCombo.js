Ext.define('Isu.view.workspace.issues.component.UserCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-user-combo',
    store: 'Isu.store.Users',
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
    typeAhead: true,
    anchor: '100%',
    forceSelection: true,
    formBind: false,
    emptyText: 'select user',
    listConfig: {
        getInnerTpl: function(displayField) {
            return '<img src="../../apps/issue/resources/images/icons/USER.png"/> {' + displayField + '}';
        }
    }
});



Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-assignee-combo',
    store: 'Isu.store.Assignee',
    displayField: 'title',
    valueField: 'id',
    queryMode: 'remote',
    queryParam: 'like',
    formBind: true,
    width: '100%',

    listConfig: {
        getInnerTpl: function () {
            return '<tpl if="type"><span class="isu-icon-{type}"></span></tpl> {title}';
        }
    }
});

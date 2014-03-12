Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.issues-assignee-combo',

    name: 'assignee',
    displayField: 'title',
    valueField: 'id',
    grow: true,
    store: 'Isu.store.Assignee',
    queryMode: 'local',
    fieldLabel: 'Assignee',
    labelAlign : 'top',
    formBind: true,

    listConfig: {
        getInnerTpl: function () {
            return '<tpl if="type"><span class="isu-icon-{type}"></span></tpl> {title}';
        }
    }
});

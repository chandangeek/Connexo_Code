Ext.define('Isu.view.workspace.issues.component.AssigneeCombo', {
    extend: 'Ext.form.field.ComboBox',
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
    hideTrigger:true,
    anchor: '100%',
    emptyText: 'type something',

    constructor: function (args) {
        var me = this,
            groupField =  args.groupField || me.groupField,
            groupDisplayField = args.groupDisplayField || me.groupDisplayField,
            displayField = args.displayField || me.displayField;

        args.tpl = new Ext.XTemplate(
            '<tpl for=".">',
            '<tpl if="this.' + groupField + ' != values.' + groupField + '">',
            '<tpl exec="this.' + groupField + ' = values.' + groupField + '"></tpl>',
            '<div class="x-panel-header-default x-panel-header-text-container x-panel-header-text x-panel-header-text-default" title="{' + groupDisplayField + '}">{' + groupDisplayField + '}</div>',
            '</tpl>',
            '<div class="x-boundlist-item">{' + displayField + '}</div>',
            '</tpl>'
        );

        me.callParent(arguments);
    },
    pageSize: 10
});


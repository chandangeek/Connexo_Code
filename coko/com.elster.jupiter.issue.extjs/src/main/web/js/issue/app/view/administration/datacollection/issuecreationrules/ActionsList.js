Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action'
    ],
    alias: 'widget.issues-creation-rules-actions-list',
    store: Ext.create('Ext.data.Store', {
        fields: [
            {
                name: 'id',
                type: 'int'
            },
            {
                name: 'type',
                type: 'auto'
            },
            {
                name: 'phase',
                type: 'auto'
            },
            {
                name: 'parameters',
                type: 'auto'
            }
        ]
    }),
    enableColumnHide: false,
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'description',
                header: 'Description',
                xtype: 'templatecolumn',
                tpl: '{type.name}',
                flex: 1
            },
            {
                itemId: 'phase',
                header: 'When to perform',
                xtype: 'templatecolumn',
                tpl: new Ext.XTemplate('{[this.getWhenToPerform(values.phase.uuid)]}', {
                    getWhenToPerform: function (uuid) {
                        var phasesStore = Ext.getStore('Isu.store.CreationRuleActionPhases'),
                            whenToPerform = phasesStore.getById(uuid).get('title');

                        return (whenToPerform || '');
                    }
                }),
                flex: 1
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: [
                    {
                        text: 'Delete',
                        action: 'delete'
                    }
                ]
            }
        ]
    }
});
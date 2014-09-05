Ext.define('Isu.view.administration.datacollection.issuecreationrules.ActionsList', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Isu.model.CreationRuleAction'
    ],
    alias: 'widget.issues-creation-rules-actions-list',
    store: Ext.create('Ext.data.Store', {
        model: 'Isu.model.CreationRuleAction'
    }),
    enableColumnHide: false,
    columns: {
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
                        text: Uni.I18n.translate('general.remove', 'ISE', 'Remove'),
                        action: 'delete'
                    }
                ]
            }
        ]
    }
});
Ext.define('Imt.usagepointlifecyclestates.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usagepoint-life-cycle-states-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'state-name-field',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                name: 'name'
            },
            {
                itemId: 'is-initial-field',
                fieldLabel: Uni.I18n.translate('usagePointLifeCycleStates.initialState', 'IMT', 'Initial state'),
                name: 'isInitial',
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.no', 'IMT', 'No')
                }
            },
            {
                itemId: 'stage-field',
                fieldLabel: Uni.I18n.translate('general.stage', 'IMT', 'Stage'),
                name: 'stage',
                renderer: function (value) {
                    var stage = Ext.getStore('Imt.usagepointlifecycle.store.Stages').getById(value);

                    return stage ? stage.get('name') : value;
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'entry-field-container',
                fieldLabel: Uni.I18n.translate('transitionBusinessProcess.entry', 'IMT', 'Processes on entry'),
                hidden: true, //temporarily
                items: [
                    {
                        xtype: 'container',
                        itemId: 'entry-container'
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'exit-field-container',
                fieldLabel: Uni.I18n.translate('transitionBusinessProcess.exit', 'IMT', 'Processes on exit'),
                hidden: true, //temporarily
                items: [
                    {
                        xtype: 'container',
                        itemId: 'exit-container'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});

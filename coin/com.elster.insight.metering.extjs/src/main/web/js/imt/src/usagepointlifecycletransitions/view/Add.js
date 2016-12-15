Ext.define('Imt.usagepointlifecycletransitions.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycle-transitions-add',
    router: null,
    edit: false,

    requires: [
        'Imt.usagepointlifecycletransitions.view.widget.StatePropertiesForm',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionAutoActions',
        'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionChecks'
    ],

    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.save', 'IMT', 'Save'));
            this.down('#add-button').action = 'editTransition';
        } else {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.add', 'IMT', 'Add'));
            this.down('#add-button').action = 'addTransition';
        }
    },

    fillActionsAndChecks: function () {
        var me = this,
            autoActionsContainer = me.down('#autoActionsContainer'),
            pretransitionChecksContainer = me.down('#pretransitionsContainer'),
            fromComboValue = me.down('#transition-from-combo').getValue(),
            toComboValue = me.down('#transition-to-combo').getValue();

        if (!!fromComboValue && !!toComboValue) {
            me.fillActions(fromComboValue, toComboValue);
            me.fillChecks(fromComboValue, toComboValue);
        } else {
            Ext.suspendLayouts();
            autoActionsContainer.removeAll();
            autoActionsContainer.hide();
            pretransitionChecksContainer.removeAll();
            pretransitionChecksContainer.hide();
            Ext.resumeLayouts(true);
        }
    },

    fillActions: function (fromComboValue, toComboValue) {
        var me = this,
            autoActionsContainer = me.down('#autoActionsContainer'),
            store = Ext.create('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionAutoActions');

        store.getProxy().setUrl(fromComboValue, toComboValue);

        store.load(
            function () {
                Ext.suspendLayouts();
                autoActionsContainer.removeAll();
                autoActionsContainer.add({
                    xtype: 'state-properties-form',
                    propertiesStore: this,
                    itemId: 'actions-property-form',
                    margin: '0 0 -20 0',
                    itemStack: 'actions'
                });
                autoActionsContainer.show();
                Ext.resumeLayouts(true);
                autoActionsContainer.fireEvent('rendered', autoActionsContainer);
            }
        );
    },

    fillChecks: function (fromComboValue, toComboValue) {
        var me = this,
            pretransitionChecksContainer = this.down('#pretransitionsContainer'),
            store = Ext.create('Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionChecks');

        store.getProxy().setUrl(fromComboValue, toComboValue);

        store.load(
            function () {
                Ext.suspendLayouts();
                pretransitionChecksContainer.removeAll();
                pretransitionChecksContainer.add({
                    xtype: 'state-properties-form',
                    propertiesStore: this,
                    margin: '0 0 -20 0',
                    itemId: 'checks-property-form',
                    itemStack: 'checks'
                });
                pretransitionChecksContainer.show();
                Ext.resumeLayouts(true);
                pretransitionChecksContainer.fireEvent('rendered', pretransitionChecksContainer);
            }
        );
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.addTransition', 'IMT', 'Add transition'),
                itemId: 'usagepoint-life-cycle-transitions-add-form',
                ui: 'large',
                width: '100%',
                defaults: {
                    labelWidth: 250
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'form-errors',
                        margin: '0 0 20 20',
                        hidden: true,
                        width: 800
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'transition-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.from', 'IMT', 'From'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'transition-from-combo',
                                name: 'fromState',
                                width: 235,
                                store: 'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionFromState',
                                editable: false,
                                lastQuery: '',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                listeners: {
                                    select: function (combo, chosenState) {
                                        var transitionToCombo = me.down('#transition-to-combo'),
                                            store = transitionToCombo.getStore(),
                                            id;

                                        me.fillActionsAndChecks();
                                        transitionToCombo.getStore().filterBy(function (state) {
                                            id = Ext.isArray(chosenState) ? chosenState[0].getId() : chosenState.getId();
                                            return state.getId() !== id;
                                        });
                                        if (store.getCount() == 0) {
                                            transitionToCombo.hide();
                                            transitionToCombo.nextSibling().show();
                                        }
                                    }
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                itemId: 'no-states',
                                hidden: true,
                                width: 250,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'from-no-states-field',
                                        value: Uni.I18n.translate('usagePointLifeCycleTransitions.add.noStates', 'IMT', 'There are no states available.')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '0 0 0 20',
                                        itemId: 'from-add-state-link',
                                        text: Uni.I18n.translate('general.addState', 'IMT', 'Add state'),
                                        ui: 'link'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.to', 'IMT', 'To'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'transition-to-combo',
                                name: 'toState',
                                width: 235,
                                store: 'Imt.usagepointlifecycletransitions.store.UsagePointLifeCycleTransitionToState',
                                editable: false,
                                lastQuery: '',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                listeners: {
                                    select: function (combo, chosenState) {
                                        var transitionFromCombo = me.down('#transition-from-combo'),
                                            store = transitionFromCombo.getStore(),
                                            id;

                                        me.fillActionsAndChecks();

                                        store.filterBy(function (state) {
                                            id = Ext.isArray(chosenState) ? chosenState[0].getId() : chosenState.getId();
                                            return state.getId() !== id;
                                        });

                                        if (store.getCount() == 0) {
                                            transitionFromCombo.hide();
                                            transitionFromCombo.nextSibling().show();
                                        }
                                    }
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                layout: 'hbox',
                                itemId: 'no-states',
                                hidden: true,
                                width: 250,
                                items: [
                                    {
                                        xtype: 'displayfield',
                                        itemId: 'to-no-states-field',
                                        value: Uni.I18n.translate('usagePointLifeCycleTransitions.add.noStates', 'IMT', 'There are no states available.')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'to-add-state-link',
                                        margin: '0 0 0 20',
                                        text: Uni.I18n.translate('general.addState', 'IMT', 'Add state'),
                                        ui: 'link'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('general.privileges', 'IMT', 'Privileges'),
                        itemId: 'privileges-checkboxgroup',
                        columns: 1,
                        vertical: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'pretransitionsContainer',
                        fieldLabel: Uni.I18n.translate('usagePointLifeCycleTransitions.add.pretransitionChecks', 'IMT', 'Pretransition checks'),
                        hidden: true,
                        layout: 'fit'
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'autoActionsContainer',
                        fieldLabel: Uni.I18n.translate('usagePointLifeCycleTransitions.add.autoActions', 'IMT', 'Auto actions'),
                        hidden: true,
                        layout: 'fit'
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'add-button',
                                text: Uni.I18n.translate('general.add', 'IMT', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
                                ui: 'link',
                                href: me.router.getRoute('administration/usagepointlifecycles/usagepointlifecycle/transitions').buildUrl()
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
        me.setEdit(me.edit);
    }
});

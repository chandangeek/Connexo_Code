Ext.define('Dlc.devicelifecycletransitions.view.Add', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-life-cycle-transitions-add',
    router: null,
    edit: false,

    requires: [
        "Dlc.devicelifecycletransitions.view.widget.StatePropertiesForm"
    ],

    setEdit: function (edit) {
        if (edit) {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.save', 'DLC', 'Save'));
            this.down('#add-button').action = 'editTransition';
        } else {
            this.edit = edit;
            this.down('#add-button').setText(Uni.I18n.translate('general.add', 'DLC', 'Add'));
            this.down('#add-button').action = 'addTransition';
        }
    },

    fillActionsAndChecks: function (fromComboValue, toComboValue) {
        if (!!fromComboValue && !!toComboValue && (fromComboValue != this.fromValue || this.toValue != toComboValue)) {
            this.fromValue = fromComboValue
            this.toValue = toComboValue
            this.fillActions(fromComboValue, toComboValue);
            this.fillChecks(fromComboValue, toComboValue);
        }
    },

    fillActions: function (fromComboValue, toComboValue) {
        var me = this,
            autoActionsContainer = me.down('#autoActionsContainer'),
            store = Ext.create('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionAutoActions');

        store.getProxy().setUrl(me.router.arguments, fromComboValue, toComboValue);

        store.load(
            function () {
                autoActionsContainer.removeAll();
                autoActionsContainer.add({
                    xtype: 'state-properties-form',
                    propertiesStore: this,
                    itemId: 'actions-property-form',
                    margin: '0 0 -20 0',
                    itemStack: 'actions'
                });
                autoActionsContainer.fireEvent('rendered', autoActionsContainer);
                autoActionsContainer.show();
            }
        );
    },

    fillChecks: function (fromComboValue, toComboValue) {
        var me = this,
            pretransitionChecksContainer = this.down('#pretansitionsContainer'),
            store = Ext.create('Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionChecks');

        store.getProxy().setUrl(me.router.arguments, fromComboValue, toComboValue);

        store.load(
            function () {
                pretransitionChecksContainer.removeAll();
                pretransitionChecksContainer.add({
                    xtype: 'state-properties-form',
                    propertiesStore: this,
                    margin: '0 0 -20 0',
                    itemId: 'checks-property-form',
                    itemStack: 'checks'
                });
                pretransitionChecksContainer.fireEvent('rendered', pretransitionChecksContainer);
                pretransitionChecksContainer.show();
            }
        );
    },

    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.addTransition', 'DLC', 'Add transition'),
                itemId: 'device-life-cycle-transitions-add-form',
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
                        xtype: 'combobox',
                        fieldLabel: Uni.I18n.translate('general.triggeredBy', 'DLC', 'Triggered by'),
                        itemId: 'transition-triggered-by-combo',
                        name: 'triggeredBy',
                        width: 500,
                        required: true,
                        store: 'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionEventTypes',
                        editable: false,
                        emptyText: Uni.I18n.translate('deviceLifeCycleTransitions.add.triggerDefaultMsg', 'DLC', 'Select a trigger event...'),
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'symbol'
                    },
                    {
                        xtype: 'textfield',
                        name: 'name',
                        itemId: 'transition-name',
                        width: 500,
                        required: true,
                        fieldLabel: Uni.I18n.translate('general.name', 'DLC', 'Name'),
                        enforceMaxLength: true,
                        maxLength: 80
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.from', 'DLC', 'From'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'transition-from-combo',
                                name: 'fromState',
                                width: 235,
                                store: 'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionFromState',
                                editable: false,
                                lastQuery: '',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                listeners: {
                                    select: function (combo, chosenState) {
                                        var transitionToCombo = me.down('#transition-to-combo'),
                                            transitionFromCombo = this,
                                            store = transitionToCombo.getStore(),
                                            id;

                                        me.fillActionsAndChecks(transitionFromCombo.getValue(), transitionToCombo.getValue());
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
                                        value: Uni.I18n.translate('deviceLifeCycleTransitions.add.noStates', 'DLC', 'There are no states available.')
                                    },
                                    {
                                        xtype: 'button',
                                        margin: '0 0 0 20',
                                        itemId: 'from-add-state-link',
                                        text: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                                        ui: 'link'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('general.to', 'DLC', 'To'),
                        required: true,
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'combobox',
                                itemId: 'transition-to-combo',
                                name: 'toState',
                                width: 235,
                                store: 'Dlc.devicelifecycletransitions.store.DeviceLifeCycleTransitionToState',
                                editable: false,
                                lastQuery: '',
                                queryMode: 'local',
                                displayField: 'name',
                                valueField: 'id',
                                listeners: {
                                    select: function (combo, chosenState) {
                                        var transitionFromCombo = me.down('#transition-from-combo'),
                                            transitionToCombo = this,
                                            store = transitionFromCombo.getStore(),
                                            id;

                                        me.fillActionsAndChecks(transitionFromCombo.getValue(), transitionToCombo.getValue());

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
                                        value: Uni.I18n.translate('deviceLifeCycleTransitions.add.noStates', 'DLC', 'There are no states available.')
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'to-add-state-link',
                                        margin: '0 0 0 20',
                                        text: Uni.I18n.translate('deviceLifeCycleStates.add', 'DLC', 'Add state'),
                                        ui: 'link'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('general.privileges', 'DLC', 'Privileges'),
                        itemId: 'privileges-checkboxgroup',
                        columns: 1,
                        vertical: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'pretansitionsContainer',
                        fieldLabel: Uni.I18n.translate('deviceLifeCycleTransitions.add.pretransitionChecks', 'DLC', 'Pretransition checks'),
                        hidden: true
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'autoActionsContainer',
                        fieldLabel: Uni.I18n.translate('deviceLifeCycleTransitions.add.autoActions', 'DLC', 'Auto actions'),
                        hidden: true
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
                                text: Uni.I18n.translate('general.add', 'DLC', 'Add'),
                                ui: 'action'
                            },
                            {
                                xtype: 'button',
                                itemId: 'cancel-link',
                                text: Uni.I18n.translate('general.cancel', 'DLC', 'Cancel'),
                                ui: 'link',
                                href: me.router.getRoute('administration/devicelifecycles/devicelifecycle/transitions').buildUrl()
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

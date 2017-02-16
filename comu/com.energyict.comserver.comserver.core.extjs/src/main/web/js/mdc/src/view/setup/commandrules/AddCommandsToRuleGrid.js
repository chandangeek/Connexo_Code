/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.commandrules.AddCommandsToRuleGrid', {
    extend: 'Uni.view.grid.SelectionGrid',
    alias: 'widget.AddCommandsToRuleGrid',

    requires: [
        'Mdc.store.SelectedCommands',
        'Mdc.view.setup.commandrules.SelectedCommandsWindow'
    ],

    plugins: {
        ptype: 'bufferedrenderer'
    },

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfCommandsSelected', count, 'MDC',
            'No commands selected', '{0} command selected', '{0} commands selected'
        );
    },

    bottomToolbarHidden: true,
    checkAllButtonPresent: true,
    hiddenSelection: [],
    ignoreSelectEvent: false,

    columns: [
        {
            header: Uni.I18n.translate('general.category', 'MDC', 'Category'),
            dataIndex: 'category',
            flex: 1
        },
        {
            header: Uni.I18n.translate('general.command', 'MDC', 'Command'),
            dataIndex: 'command',
            flex: 1
        }
    ],

    initComponent: function () {
        var me = this;
        me.hiddenSelection = [];
        me.callParent(arguments);

        me.on('select', function (grid, record) {
            if (!me.ignoreSelectEvent) {
                me.addToSelection(record);
            }
        });
        me.on('deselect', function (grid, record) {
            if (!me.ignoreSelectEvent) {
                me.removeFromSelection(record);
            }
        });

        me.getTopToolbarContainer().add(1,{
            xtype: 'button',
            hidden: true,
            itemId: 'mdc-add-commands-to-rule-selectedCommands-info-btn',
            tooltip: Uni.I18n.translate('general.moreInfo.tooltip', 'MDC', 'Click for more information'),
            iconCls: 'uni-icon-info-small',
            cls: 'uni-btn-transparent',
            width: 15,
            style: {
                display: 'inline-block',
                textDecoration: 'none !important',
                position: 'absolute',
                top: '5px'
            },
            handler: function () {
                var widget = Ext.widget('AddCommandsToRuleSelectedCommands');
                widget.setTitle(me.counterTextFn(me.hiddenSelection.length));
                widget.show();
            }
        });

        me.getStore().on('load', function () { // When (re)loaded (by applying another filter)
            // (re)select the previously chosen commands and update the buttons correspondingly
            if (me.getSelectedItems().length > 0) {
                me.ignoreSelectEvent = true;
                var commandsToReselect = [];
                Ext.defer(function () {
                    Ext.each(me.hiddenSelection, function (command) {
                        var indexInStore = me.getStore().findExact('commandName', command.get('commandName'));
                        record = indexInStore === -1 ? null : me.getStore().getAt(indexInStore);
                        if (record) {
                            commandsToReselect.push(record);
                        }
                    });
                    me.getSelectionModel().select(commandsToReselect);
                    me.ignoreSelectEvent = false;
                    me.updateButtons();
                }, 100);
            } else {
                me.updateButtons();
            }
        });
    },

    updateButtons: function() { // for the current selection
        var me = this;
        if (!me.rendered) {
            return;
        }
        var selection = me.getSelectionModel().getSelection();
        me.getUncheckAllButton().setDisabled(selection.length === 0);
        if (me.checkAllButtonPresent) {
            me.getCheckAllButton().setDisabled(me.getStore().getCount() === selection.length);
        }
        me.up('AddCommandsToRuleView').down('#mdc-command-rule-add-commands-addButton').setDisabled(me.getSelectedItems().length === 0);
    },

    addToSelection: function (record) {
        var index = -1;
        Ext.each(this.hiddenSelection, function (rec, ind) {
            if (rec.get('commandName') === record.get('commandName')) {
                index = ind;
            }
        });
        if (index < 0) {
            this.hiddenSelection.push(record);
        }
    },

    removeFromSelection: function (record) {
        var index = -1;
        Ext.each(this.hiddenSelection, function (rec, ind) {
            if (rec.get('commandName') === record.get('commandName')) {
                index = ind;
            }
        });
        if (index > -1) {
            this.hiddenSelection.splice(index, 1);
        }
    },

    getSelectedItems: function() {
        return this.hiddenSelection
    },

    clearSelection: function() {
        this.hiddenSelection = [];
    },

    onSelectionChange: function () {
        var me = this,
            selectionInfoBtn = me.down('#mdc-add-commands-to-rule-selectedCommands-info-btn'),
            selectedStore = Ext.getStore('Mdc.store.SelectedCommands');

        selectedStore.removeAll();
        selectedStore.add(me.hiddenSelection);
        selectionInfoBtn.setVisible(me.hiddenSelection.length>0);
        me.up('AddCommandsToRuleView').down('#mdc-command-rule-add-commands-addButton').setDisabled(this.getSelectedItems().length === 0);
        me.callParent(arguments);
    },

    onClickUncheckAllButton: function (button) {
        var me = this;
        me.callParent(arguments);
        me.onSelectionChange();
        me.updateButtons();
    }

});
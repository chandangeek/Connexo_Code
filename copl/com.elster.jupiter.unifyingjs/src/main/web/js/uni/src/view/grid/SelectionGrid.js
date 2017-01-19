/**
 * @class Uni.view.grid.SelectionGrid
 */
Ext.define('Uni.view.grid.SelectionGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'selection-grid',

    bottomToolbarHeight: 27,

    selType: 'checkboxmodel',
    selModel: {
        mode: 'MULTI',
        checkOnly: true,
        showHeaderCheckbox: false,
        pruneRemoved: false,
        updateHeaderState: Ext.emptyFn
    },

    cls: 'uni-selection-grid',

    overflowY: 'auto',
    maxHeight: 450,

    extraTopToolbarComponent: undefined,

    /**
     * @cfg counterTextFn
     *
     * The translation function to use to translate the selected count on top of the
     * text above the grid.
     *
     * @param {Number} count Count to base the translation on.
     * @returns {String} Translation value based on the count.
     */
    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('multiselect.selected', count, 'UNI',
            'No items selected', '{0} item selected', '{0} items selected'
        );
    },

    /**
     * @cfg uncheckText
     *
     * Text used for the uncheck all button.
     */
    uncheckText: Uni.I18n.translate('general.uncheckAll', 'UNI', 'Uncheck all'),
    checkText: Uni.I18n.translate('general.checkAll', 'UNI', 'Check all'),
    checkAllButtonPresent: false,

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'toolbar',
                dock: 'top',
                itemId: 'topToolbarContainer',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                items: [
                    {
                        xtype: 'component',
                        itemId: 'selectionCounter',
                        style: {
                            color: '#686868'
                        },
                        html: Ext.String.htmlEncode(me.counterTextFn(0)),
                        margin: '0 8 0 0',
                        setText: function (text) {
                            this.update(text);
                        }
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'checkAllButton',
                                text: me.checkText,
                                action: 'checkAll',
                                margin: '0 0 0 8',
                                hidden: !me.checkAllButtonPresent
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'uncheckAllButton',
                                text: me.uncheckText,
                                action: 'uncheckAll',
                                margin: '0 0 0 8',
                                disabled: true
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.getUncheckAllButton().on('click', me.onClickUncheckAllButton, me);
        if (me.checkAllButtonPresent) {
            me.getCheckAllButton().on('click', me.onClickCheckAllButton, me);
        }

        me.on('selectionchange', me.onSelectionChange, me);

        me.addComponentInToolbar();
    },

    onClickUncheckAllButton: function (button) {
        var me = this;

        me.view.getSelectionModel().deselectAll();
        button.setDisabled(true);
    },

    onClickCheckAllButton: function (button) {
        var me = this;

        me.view.getSelectionModel().selectAll();
        button.setDisabled(true);
    },

    onSelectionChange: function () {
        var me = this,
            selection = me.getSelectedItems();

        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.getUncheckAllButton().setDisabled(selection.length === 0);
        if (me.checkAllButtonPresent) {
            me.getCheckAllButton().setDisabled(me.getStore().getCount() === selection.length);
        }
    },

    getSelectionCounter: function () {
        return this.down('#selectionCounter');
    },

    getUncheckAllButton: function () {
        return this.down('#uncheckAllButton');
    },

    getCheckAllButton: function () {
        return this.down('#checkAllButton');
    },

    getTopToolbarContainer: function () {
        return this.down('#topToolbarContainer');
    },

    addComponentInToolbar: function () {
        var me = this;

        if (me.extraTopToolbarComponent) {
            me.getTopToolbarContainer().add(
                me.extraTopToolbarComponent
            )
        }
    },

    getSelectedItems: function() {
        return this.view.getSelectionModel().getSelection();
    }
});
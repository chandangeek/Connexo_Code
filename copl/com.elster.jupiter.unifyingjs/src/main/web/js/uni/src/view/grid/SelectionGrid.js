/**
 * @class Uni.view.grid.SelectionGrid
 */
Ext.define('Uni.view.grid.SelectionGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'selection-grid',

    requires: [
        'Ext.grid.plugin.BufferedRenderer'
    ],

    bottomToolbarHeight: 27,

    selType: 'checkboxmodel',
    selModel: {
        mode: 'MULTI',
        showHeaderCheckbox: false
    },

    overflowY: 'auto',
    maxHeight: 450,

    plugins: [
        {
            ptype: 'bufferedrenderer',
            trailingBufferZone: 5,
            leadingBufferZone: 5,
            scrollToLoadBuffer: 10,
            onViewResize: function (view, width, height, oldWidth, oldHeight) {
                if (height === 0 || oldHeight === 0) {
                    var me = this,
                        count = view.all.getCount(),
                        newHeight = count * me.rowHeight;

                    if (count > 10) {
                        newHeight = 10 * me.rowHeight;
                    }

                    if (view.getHeight() !== 0 && view.getHeight() !== newHeight) {
                        view.setHeight(newHeight);
                    }
                }
            }
        }
    ],

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
        return Uni.I18n.translatePlural(
            'grid.BulkSelection.counterText',
            count,
            'UNI',
            '{0} items selected'
        );
    },

    /**
     * @cfg uncheckText
     *
     * Text used for the uncheck all button.
     */
    uncheckText: Uni.I18n.translate('general.uncheckAll', 'UNI', 'Uncheck all'),

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
                        xtype: 'text',
                        itemId: 'selectionCounter',
                        text: me.counterTextFn(0),
                        margin: '0 8 0 0'
                    },
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
        ];

        me.callParent(arguments);

        me.getUncheckAllButton().on('click', me.onClickUncheckAllButton, me);
        me.on('selectionchange', me.onSelectionChange, me);
    },

    onClickUncheckAllButton: function (button) {
        var me = this;

        me.view.getSelectionModel().deselectAll();
        button.setDisabled(true);
    },

    onSelectionChange: function () {
        var me = this,
            selection = me.view.getSelectionModel().getSelection();

        me.getSelectionCounter().setText(me.counterTextFn(selection.length));
        me.getUncheckAllButton().setDisabled(selection.length === 0);
        me.doLayout();
    },

    getSelectionCounter: function () {
        return this.down('#selectionCounter');
    },

    getUncheckAllButton: function () {
        return this.down('#uncheckAllButton');
    },

    getTopToolbarContainer: function () {
        return this.down('#topToolbarContainer');
    }
});
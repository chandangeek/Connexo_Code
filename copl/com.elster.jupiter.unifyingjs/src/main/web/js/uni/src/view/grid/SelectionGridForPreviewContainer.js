/**
 * @class Uni.view.grid.SelectionGridForPreviewContainer
 *
 * This component can be used instead of {@link Uni.view.grid.SelectionGrid}
 * when selection grid is used via {@link Uni.view.container.PreviewContainer}
 */
Ext.define('Uni.view.grid.SelectionGridForPreviewContainer', {
    extend: 'Ext.grid.Panel',
    requires: [
        'Uni.grid.column.Check'
    ],
    alias: 'widget.selection-grid-for-preview-container',
    cls: 'uni-selection-grid',

    /**
     * @cfg {String} [isCheckedFieldName=isChecked]
     *
     * Name of model field to define that record is checked.
     */
    isCheckedFieldName: 'isChecked',

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
     * @cfg {String} [uncheckText=Uncheck all]
     *
     * Text used for the uncheck all button.
     */
    uncheckText: Uni.I18n.translate('general.uncheckAll', 'UNI', 'Uncheck all'),

    /**
     * @cfg {String} [checkText=Check all]
     *
     * Text used for the check all button.
     */
    checkText: Uni.I18n.translate('general.checkAll', 'UNI', 'Check all'),

    /**
     * @cfg {Boolean} [checkAllButtonPresent=false]
     *
     * Availability of the check all button.
     */
    checkAllButtonPresent: false,

    /**
     * @cfg {String} [addText=Add]
     *
     * Text used for the add button.
     */
    addText: Uni.I18n.translate('general.add', 'UNI', 'Add'),

    /**
     * @cfg {String} [cancelText=Cancel]
     *
     * Text used for the cancel button.
     */
    cancelText: Uni.I18n.translate('general.cancel', 'UNI', 'Cancel'),

    /**
     * @cfg {String} [cancelHref=null]
     *
     * Link for the cancel button.
     */
    cancelHref: null,

    /**
     * @cfg {Boolean} [bottomToolbarHidden=false]
     *
     * Availability of the bottom toolbar.
     */
    bottomToolbarHidden: false,

    initComponent: function () {
        var me = this,
            store;

        me.addEvents(
            /**
             * @event addSelected
             * Fires when Add button is clicked
             *
             * @param {Ext.grid.Panel} grid This grid.
             * @param {Ext.data.Model[]} records Array of the selected records.
             */
            'addSelected'
        );

        me.columns.splice(0, 0, {
            xtype: 'uni-checkcolumn',
            dataIndex: me.isCheckedFieldName
        });

        me.dockedItems = [
            {
                dock: 'top',
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                padding: '0 0 10 0',
                items: [
                    {
                        xtype: 'component',
                        itemId: 'selectionCounter',
                        style: 'color: #686868',
                        html: Ext.String.htmlEncode(me.counterTextFn(0)),
                        margin: '0 8 0 0'
                    },
                    {
                        xtype: 'button',
                        itemId: 'checkAllButton',
                        text: me.checkText,
                        action: 'checkAll',
                        margin: '0 0 0 8',
                        privileges: me.checkAllButtonPresent,
                        handler: Ext.bind(me.triggerAllSelection, me, [true])
                    },
                    {
                        xtype: 'button',
                        itemId: 'uncheckAllButton',
                        text: me.uncheckText,
                        action: 'uncheckAll',
                        margin: '0 0 0 8',
                        disabled: true,
                        handler: Ext.bind(me.triggerAllSelection, me, [false])
                    }
                ]
            },
            {
                dock: 'bottom',
                privileges: !me.bottomToolbarHidden,
                padding: '10 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'addButton',
                        text: me.addText,
                        action: 'add',
                        ui: 'action',
                        disabled: true,
                        handler: Ext.bind(me.onAddClick, me)
                    },
                    {
                        xtype: 'button',
                        itemId: 'cancelButton',
                        text: me.cancelText,
                        action: 'cancel',
                        ui: 'link',
                        href: me.cancelHref
                    }
                ]
            }
        ];

        me.callParent(arguments);

        store = me.getStore();
        store.on('update', me.onSelectionChange, me);
        me.on('destroy', function () {
            store.un('update', me.onSelectionChange, me);
        }, me, {single: true});
    },

    triggerAllSelection: function (flag) {
        var me = this;

        Ext.suspendLayouts();
        me.getStore().each(triggerFn);
        Ext.resumeLayouts(true);

        function triggerFn(record) {
            record.set(me.isCheckedFieldName, flag);
        }
    },

    // private
    onSelectionChange: function (store, record, operation, modifiedFieldNames) {
        var me = this,
            selectedRecords;

        if (_.contains(modifiedFieldNames, me.isCheckedFieldName)) {
            selectedRecords = me.getSelectedRecords();
            Ext.suspendLayouts();
            me.down('#selectionCounter').update(me.counterTextFn(selectedRecords.length));
            me.down('#uncheckAllButton').setDisabled(!selectedRecords.length);
            me.down('#checkAllButton').setDisabled(store.getCount() === selectedRecords.length);
            if (!me.bottomToolbarHidden) {
                me.down('#addButton').setDisabled(!selectedRecords.length);
            }
            Ext.resumeLayouts(true);
        }
    },

    getSelectedRecords: function () {
        var me = this;

        return _.filter(me.getStore().getRange(), filterFn);

        function filterFn(record) {
            return record.get(me.isCheckedFieldName);
        }
    },

    // private
    onAddClick: function () {
        var me = this;

        me.fireEvent('addSelected', me, me.getSelectedRecords());
    }
});
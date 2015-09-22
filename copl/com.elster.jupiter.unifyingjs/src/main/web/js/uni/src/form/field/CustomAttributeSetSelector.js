/**
 * @class Uni.form.field.CustomAttributeSetSelector
 */
Ext.define('Uni.form.field.CustomAttributeSetSelector', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.custom-attribute-set-selector',
    name: 'customAttributeSet',
    fieldLabel: Uni.I18n.translate('general.customattributeset', 'UNI', 'Custom attribute set'),
    comboWidth: 300,
    comboStore: null,
    lessAttributes: 3,
    emptyRecord: true,

    requires: [
        'Uni.view.form.CustomAttributeSetDetails'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'combobox',
                width: me.comboWidth,
                displayField: 'name',
                valueField: 'id',
                forceSelection: true,
                queryMode: 'local',
                editable: false,
                listeners: {
                    select: function (combo, records) {
                        me.onSelectAttributeSet(combo, records[0]);
                    }
                }
            },
            {
                xtype: 'panel',
                hidden: true,
                itemId: 'custom-attribute-set-details-panel-id',
                ui: 'tile',
                items: [
                    {
                        xtype: 'custom-attribute-set-details-form'
                    },
                    {
                        xtype: 'button',
                        itemId: 'custom-attribute-set-selector-show-less',
                        text: Uni.I18n.translate('general.showMore', 'UNI', 'Show less'),
                        handler: function () {
                            me.showLessAttributes();
                        }
                    },
                    {
                        xtype: 'button',
                        itemId: 'custom-attribute-set-selector-show-more',
                        text: Uni.I18n.translate('general.showMore', 'UNI', 'Show more'),
                        handler: function () {
                            me.showMoreAttributes();
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);

        if (me.comboStore) {
            var comboStore = Ext.create(me.comboStore);
            me.down('combobox').bindStore(comboStore);
            comboStore.on('load', function () {
                if (!this.getById(0) && me.emptyRecord) {
                    var model = Ext.ModelManager.getModel(this.model);
                    this.insert(0, new model({id: 0, name: Uni.I18n.translate('customattributeset.none', 'UNI', '(none)')}));
                }
            });
        }
    },

    showLessAttributes: function () {
        var me = this;

        me.manageButtons(true, false);
        me.down('#custom-attribute-set-details-panel-id').show();
        me.down('custom-attribute-set-details-form').loadCustomRecord(me.cutAttributes(me.selectedRecord));
    },

    showMoreAttributes: function () {
        var me = this;

        this.manageButtons(false, true);
        me.down('custom-attribute-set-details-form').loadCustomRecord(me.selectedRecord.getData());
    },

    cutAttributes: function (record) {
        var me = this,
            recordData = record.getData(),
            recordAttributes = recordData.attributes,
            attributesCount = recordAttributes.length,
            lessAttributes;

        if (attributesCount > me.lessAttributes) {
            me.down('#custom-attribute-set-selector-show-more').enable();
            lessAttributes = recordAttributes.slice(0, me.lessAttributes);
            recordData.attributes = lessAttributes;
        } else {
            me.down('#custom-attribute-set-selector-show-more').disable();
        }

        return recordData;
    },

    onSelectAttributeSet: function (combo, record) {
        var me = this;

        if (record && record.get('id')) {
            me.selectedRecord = record;
            me.showLessAttributes();
        } else {
            me.down('#custom-attribute-set-details-panel-id').hide();
            me.selectedRecord = null;
        }
    },

    setValue: function (id) {
        var me = this,
            combo = me.down('combobox');

        if (Ext.isEmpty(id)) {
            id = 0;
        }

        combo.getStore().load(function () {
            combo.setValue(id);
            combo.fireEvent('select', combo, [combo.getStore().getById(id)]);
        });
    },

    loadStore: function () {
        this.down('combobox').getStore().load();
    },

    getValue: function () {
        if (this.selectedRecord) {
            return this.selectedRecord.get('id');
        } else {
            return null
        }
    },

    manageButtons: function (showMore, showLess) {
        this.down('#custom-attribute-set-selector-show-more').setVisible(showMore);
        this.down('#custom-attribute-set-selector-show-less').setVisible(showLess);
    }
});
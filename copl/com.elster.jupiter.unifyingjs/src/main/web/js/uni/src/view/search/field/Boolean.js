Ext.define('Uni.view.search.field.Boolean', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-search-criteria-boolean',
    text: Uni.I18n.translate('view.search.field.yesno.label', 'UNI', 'Text'),
    minWidth: 70,

    getValue: function () {
        return this.value ? [Ext.create('Uni.model.search.Value', {
            operator: '==',
            criteria: this.value
        })] : null;
    },

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'radiofield', boxLabel: Uni.I18n.translate('general.yes', 'UNI', 'Yes'), name: 'bool', inputValue: "1", itemId: 'radio-yes', checked: true
            },
            {
                xtype: 'menuseparator'
            },
            {
                xtype: 'radiofield', boxLabel: Uni.I18n.translate('general.no', 'UNI', 'No'), name: 'bool', inputValue: "0", itemId: 'radio-no'
            }
        ];

        me.menuConfig = {
            minWidth: 70,
            listeners: {
                click: function (menu, item) {
                    me.setValue(item.inputValue);
                }
            }
        };

        this.callParent(arguments);
        me.setValue(1);
    }
});
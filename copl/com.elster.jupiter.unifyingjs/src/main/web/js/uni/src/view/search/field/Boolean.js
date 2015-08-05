Ext.define('Uni.view.search.field.Boolean', {
    extend: 'Uni.view.search.field.internal.CriteriaButton',
    xtype: 'uni-view-search-field-yesno',
    text: Uni.I18n.translate('view.search.field.yesno.label', 'FWC', 'Text'),
    emptyText: Uni.I18n.translate('view.search.field.yesno.label', 'FWC', 'Text'),
    minWidth: 70,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'radiofield', boxLabel: Uni.I18n.translate('window.messabox.yes', 'UNI', 'Yes'), name: 'bool', inputValue: true, itemId: 'radio-yes', checked: true
            },
            {
                xtype: 'menuseparator'
            },
            {
                xtype: 'radiofield', boxLabel: Uni.I18n.translate('window.messabox.no', 'UNI', 'No'), name: 'bool', inputValue: false, itemId: 'radio-no'
            }
        ];

        me.menuConfig = {
            minWidth: 70,
            listeners: {
                click: function (menu, item) {
                    me.onChange(me, item.inputValue);
                }
            }
        };

        this.callParent(arguments);
    }

});
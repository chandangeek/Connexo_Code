Ext.define('Uni.view.search.field.YesNo', {
    extend: 'Ext.button.Button',
    xtype: 'uni-view-search-field-yesno',
    text: Uni.I18n.translate('view.search.field.yesno.label', 'FWC', 'Text'),
    defaultText: Uni.I18n.translate('view.search.field.yesno.label', 'FWC', 'Text'),
    textAlign: 'left',
    arrowAlign: 'right',
    menuAlign: 'tl-bl',
    layout: 'hbox',
    minWidth: 70,
    style: {
        'background-color': '#71adc7'
    },

    initComponent: function () {
        me = this;
        me.menu = {
            plain: true,
            style: {
                overflow: 'visible'
            },
            cls: 'x-menu-body-custom',
            minWidth: 70,
            items: [
                {
                    xtype: 'fieldcontainer',
                    defaultType: 'radiofield',
                    flex: 1,
                    layout: 'hbox',
                    items: [
                        {boxLabel: Uni.I18n.translate('window.messabox.yes', 'UNI', 'Yes'), name: 'bool', inputValue: true, itemId: 'radio-yes', checked: true}
                    ]
                },
                {
                    xtype: 'menuseparator'
                },
                {
                    xtype: 'fieldcontainer',
                    defaultType: 'radiofield',
                    flex: 1,
                    layout: 'hbox',
                    items: [
                        {boxLabel: Uni.I18n.translate('window.messabox.no', 'UNI', 'No'), name: 'bool', inputValue: true, itemId: 'radio-no'}
                    ]
                }
            ],
            listeners: {
                click: function (menu) {
                    var edited = false;
                    menu.items.each(function (item) {

                        if (item.xtype != 'menuseparator') {
                            if(item.down('radiofield').checked) edited = true
                        }
                    });
                    var button = menu.up('uni-view-search-field-yesno');
                    if (edited) {
                        button.setText(button.defaultText + '*');
                    } else {
                        button.setText(button.defaultText);
                    }
                }
            }
        }
        this.callParent(arguments);
    }

});
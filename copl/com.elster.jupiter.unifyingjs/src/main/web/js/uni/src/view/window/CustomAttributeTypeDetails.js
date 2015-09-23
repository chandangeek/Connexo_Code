Ext.define('Uni.view.window.CustomAttributeTypeDetails', {
    extend: 'Ext.window.Window',
    xtype: 'custom-attribute-type-details',
    closable: true,
    width: 400,
    constrain: true,
    autoShow: true,
    modal: true,
    layout: 'fit',
    closeAction: 'destroy',
    floating: true,
    possibleValues: null,

    initComponent: function() {
        var me = this,
            possibleItems = [{
                xtype: 'container',
                margin : '0 0 20 0',
                html: Uni.I18n.translate('customattributetype.description', 'UNI', 'This attribute could contain one of following values:')
            }];

        Ext.each(me.possibleValues, function(value) {
            possibleItems.push({
                xtype: 'container',
                margin : '0 0 5 15',
                html: '- ' + value
            });
        });

        me.items = [{
            xtype:'panel',
            ui: 'medium',
            title: Uni.I18n.translate('customattributetype.title', 'UNI', 'Attribute details'),
            items: possibleItems
        }];

        this.callParent(arguments);
    }
});
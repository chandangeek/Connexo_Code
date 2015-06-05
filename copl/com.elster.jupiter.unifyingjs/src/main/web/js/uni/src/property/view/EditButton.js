Ext.define('Uni.property.view.EditButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-edit-button',

    border: 0,
    iconCls: 'icon-pencil5',
    iconAlign: 'center',
    height: 28,
    width: 28,
    action: 'edit',
    margin: '0 0 0 5',
    hidden: true
});
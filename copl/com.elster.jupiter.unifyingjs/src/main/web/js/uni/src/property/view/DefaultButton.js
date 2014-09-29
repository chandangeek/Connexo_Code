Ext.define('Uni.property.view.DefaultButton', {
    extend: 'Ext.button.Button',
    xtype: 'uni-default-button',

    border: 0,
    icon: '../sky/build/resources/images/form/restore.png',
    height: 28,
    width: 28,
    scale: 'small',
    action: 'delete',
    margin: '0 0 5 5',
    hidden: true
});
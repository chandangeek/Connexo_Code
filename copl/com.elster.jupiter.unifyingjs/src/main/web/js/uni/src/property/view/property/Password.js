Ext.define('Uni.property.view.property.Password', {
    extend: 'Uni.property.view.property.Text',

    getNormalCmp: function () {
        var result = this.callParent(arguments);
        result.inputType = 'password';

        return result;
    }
});
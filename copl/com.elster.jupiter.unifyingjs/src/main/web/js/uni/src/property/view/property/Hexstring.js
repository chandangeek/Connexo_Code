Ext.define('Uni.property.view.property.Hexstring', {
    extend: 'Uni.property.view.property.Text',

    getNormalCmp: function () {
        var result = this.callParent(arguments);
        result.vtype = 'hexstring';

        return result;
    }
});
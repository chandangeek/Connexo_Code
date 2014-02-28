Ext.define('Mtr.view.person.List', {
    extend: 'Mtr.view.party.List',
    alias: 'widget.personList',
    itemId: 'personList',
    title: 'All persons',
    store: 'Persons',
    columns: {
        defaults: {
            flex: 1
        },
        items: [
            { header: 'Id', dataIndex: 'id' },
            { header: 'MRID', dataIndex: 'mRID' },
            { header: 'Name', dataIndex: 'name' },
            { header: 'Alias name', dataIndex: 'aliasName' },
            { header: 'First name', dataIndex: 'firstName' },
            { header: 'Last name', dataIndex: 'lastName' },
            { header: 'Middle name', dataIndex: 'mName' },
            { header: 'Prefix', dataIndex: 'prefix' },
            { header: 'Suffix', dataIndex: 'suffix' },
            { header: 'Special need', dataIndex: 'specialNeed' }
        ]
    },
    initComponent: function () {
        this.callParent(arguments);
    }
});
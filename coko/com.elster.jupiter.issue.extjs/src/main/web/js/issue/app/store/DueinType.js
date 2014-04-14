Ext.define('Isu.store.DueinType', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.DueinType',

    data: [
        {"name": "days"},
        {"name": "weeks"},
        {"name": "months"}
    ]
});

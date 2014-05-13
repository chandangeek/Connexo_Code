Ext.define('Uni.override.FormOverride', {
    override: 'Ext.form.Basic',
    hydrator: null,

    constructor: function(owner) {
        this.callParent(arguments);
        if (owner.hydrator) {
            this.setHydrator(Ext.create(owner.hydrator))
        }
    },

    setHydrator: function(hydrator) {
        this.hydrator = hydrator
    },

    loadRecord: function(record) {
        if (!this.hydrator) {
            this.callParent(arguments)
        } else {
            this._record = record;
            return this.setValues(this.hydrator.extract(record));
        }
    },

    updateRecord: function(record) {
        if (this.hydrator) {
            this.hydrator.lazyLoading = false;
            var values = this.getValues();
            this.hydrator.hydrate(values, this._record, function() {});
            return this;
        } else {
            return this.callParent(arguments);
        }
    }
});
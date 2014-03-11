describe('Initial assumptions', function () {
    it('has ExtJS 4 loaded', function () {
        expect(Ext).toBeDefined();
        expect(Ext.getVersion()).toBeTruthy();
        expect(Ext.getVersion().major).toEqual(4);
    });

    it('has loaded Isu code', function () {
        expect(Isu).toBeDefined();
    });
});
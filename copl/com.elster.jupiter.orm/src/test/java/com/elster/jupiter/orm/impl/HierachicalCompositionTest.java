/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

public class HierachicalCompositionTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Before
    public void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UtilModule(),
                new ThreadSecurityModule(() -> "test"),
                new PubSubModule(),
                new TransactionModule(false),
                new OrmModule());

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            OrmService service = injector.getInstance(OrmService.class);
            DataModel ormDataModel = service.newDataModel("ORM", "forTest");
            Arrays.stream(com.elster.jupiter.orm.internal.TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(ormDataModel));
            ormDataModel.register();
            ormDataModel.install(true, true);
            DataModel dataModel = service.newDataModel("TST", "forTest");
            Arrays.stream(TableSpecs.values())
                    .forEach(tableSpecs -> tableSpecs.addTo(dataModel));
            dataModel.register();
            dataModel.install(true, true);
            ctx.commit();
        }
    }

    @After
    public void cleanUp() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testSave() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel tst = service.getDataModel("TST").get();

        TestA a = tst.getInstance(TestA.class);
        a.init("A");
        TestC c = tst.getInstance(TestC.class);
        c.init(a, "C");

        a.addChild(c);

        TestB b = tst.getInstance(TestB.class);
        b.init("B");


        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));
        injector.getInstance(TransactionService.class).run(() -> tst.persist(b));

        List<TestA> selectA = tst.query(TestA.class, Namer.class).select(Condition.TRUE);
        assertThat(selectA).hasSize(1);
        TestA myA = selectA.get(0);
        assertThat(myA.children).hasSize(1);
        TestC myC = myA.children.get(0);
        List<TestB> selectB = tst.query(TestB.class).select(Condition.TRUE);
        assertThat(selectB).hasSize(1);

        List<Namer> select = tst.query(Namer.class).select(Condition.TRUE);
        assertThat(select).hasSize(2);
    }

    @Test
    public void testDelete() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel tst = service.getDataModel("TST").get();

        TestA a = tst.getInstance(TestA.class);
        a.init("A");
        TestC c = tst.getInstance(TestC.class);
        c.init(a, "C");
        a.addChild(c);

        TestB b = tst.getInstance(TestB.class);
        b.init("B");

        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));
        injector.getInstance(TransactionService.class).run(() -> tst.persist(b));

        List<TestA> select = tst.query(TestA.class).select(Condition.TRUE);
        assertThat(select).hasSize(1);
        assertThat(tst.query(Namer.class).select(Condition.TRUE)).hasSize(2);
        TestA myA = select.get(0);
        assertThat(myA.children).hasSize(1);
        TestC myC = myA.children.get(0);
        assertThat(myC.parent.get()).isEqualTo(myA);
        injector.getInstance(TransactionService.class).run(() -> tst.remove(myA));
        assertThat(tst.query(TestA.class).select(Condition.TRUE)).isEmpty();
        assertThat(tst.query(TestB.class).select(Condition.TRUE)).hasSize(1);
        assertThat(tst.query(Namer.class).select(Condition.TRUE)).hasSize(1);

        List<TestB> selectB = tst.query(TestB.class).select(Condition.TRUE);
        assertThat(selectB).hasSize(1);
        injector.getInstance(TransactionService.class).run(() -> tst.remove(selectB.get(0)));
        assertThat(tst.query(TestA.class).select(Condition.TRUE)).isEmpty();
        assertThat(tst.query(TestB.class).select(Condition.TRUE)).isEmpty();
        assertThat(tst.query(Namer.class).select(Condition.TRUE)).isEmpty();
    }

    @Test
    public void testDeleteChild() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel tst = service.getDataModel("TST").get();

        TestA a = tst.getInstance(TestA.class);
        a.init("A");
        TestC c = tst.getInstance(TestC.class);
        c.init(a, "C");
        a.addChild(c);

        TestB b = tst.getInstance(TestB.class);
        b.init("B");

        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));
        injector.getInstance(TransactionService.class).run(() -> tst.persist(b));

        List<TestA> select = tst.query(TestA.class).select(Condition.TRUE);
        assertThat(select).hasSize(1);
        assertThat(tst.query(Namer.class).select(Condition.TRUE)).hasSize(2);
        TestA myA = select.get(0);
        assertThat(myA.children).hasSize(1);
        TestC myC = myA.children.get(0);
        assertThat(myC.parent.get()).isEqualTo(myA);
        injector.getInstance(TransactionService.class).run(() -> myA.remove(myC));
        List<TestA> selectA = tst.query(TestA.class).select(Condition.TRUE);
        assertThat(selectA).hasSize(1);
        //assertThat(selectA.get(0).children).isEmpty();
        assertThat(tst.query(TestB.class).select(Condition.TRUE)).hasSize(1);
        assertThat(tst.query(Namer.class).select(Condition.TRUE)).hasSize(1);
    }

    @Test
    @Expected(UnsupportedOperationException.class)
    public void testUnmanageDeleteChild() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel tst = service.getDataModel("TST").get();

        TestA a = tst.getInstance(TestA.class);
        a.init("A");
        TestC c = tst.getInstance(TestC.class);
        c.init(a, "C");
        a.addChild(c);

        TestB b = tst.getInstance(TestB.class);
        b.init("B");

        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));
        injector.getInstance(TransactionService.class).run(() -> tst.persist(b));

        List<TestA> select = tst.query(TestA.class).select(Condition.TRUE);
        assertThat(select).hasSize(1);
        assertThat(tst.query(Namer.class).select(Condition.TRUE)).hasSize(2);
        TestA myA = select.get(0);
        assertThat(myA.children).hasSize(1);
        TestC myC = myA.children.get(0);
        assertThat(myC.parent.get()).isEqualTo(myA);
        injector.getInstance(TransactionService.class).run(() -> tst.remove(myC));
    }

    private enum TableSpecs {
        TEST_A {
            @Override
            public void addTo(DataModel dataModel) {
                Table<TestA> testATable = dataModel.addTable(name(), TestA.class);
                testATable.map(TestA.class);
                Column idColumn = testATable.addAutoIdColumn();
                testATable.column("NAME").varChar(80).notNull().map("name").add();
                testATable.primaryKey("TEST_A_PK_CONSTRAINT").on(idColumn).add();
            }
        },
        NAMER {
            @Override
            public void addTo(DataModel dataModel) {
                Table<Namer> testBTable = dataModel.addTable(name(), Namer.class);
                testBTable.map(ImmutableMap.of(TestB.class.getName(), TestB.class, TestC.class.getName(), TestC.class));
                Column idColumn = testBTable.addAutoIdColumn();
                testBTable.addDiscriminatorColumn("discriminator", "varchar(200)");
                testBTable.column("NAME").varChar(80).notNull().map("name").add();
                Column testa = testBTable.column("TESTA").number().add();
                testBTable.primaryKey("TEST_B_PK_CONSTRAINT").on(idColumn).add();
                testBTable.foreignKey("TEST_B_FK_TEST_A").on(testa).references(TestA.class).map("parent").reverseMap("children").composition().add();
            }
        };

        public abstract void addTo(DataModel dataModel);
    }

    private interface Namer {
        String name();

    }

    private static class TestC implements Namer {
        private long id;
        private String name;
        private Reference<TestA> parent = Reference.empty();

        public void init(TestA parent, String name) {
            this.name = name;
            this.parent.set(parent);
        }

        @Override
        public String name() {
            return name;
        }

    }

    private static class TestB implements Namer {
        private long id;
        private String name;

        public void init(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

    }

    private static class TestA {
        private long id;
        private String name;
        private List<TestC> children = new ArrayList<>();


        public void init(String name) {
            this.name = name;
        }

        public void addChild(TestC c) {
            children.add(c);
        }

        public boolean remove(TestC myB) {
            return children.remove(myB);
        }
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
        }
    }
}

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.orm.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

public class CompositionCRUDTest {

    private Injector injector;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

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
        TestB b = tst.getInstance(TestB.class);
        b.init(a, "B");
        TestC c = tst.getInstance(TestC.class);
        c.init(b, "C");

        b.addC(c);
        a.addB(b);

        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));

        List<TestA> selectA = tst.query(TestA.class, TestB.class, TestC.class).select(Condition.TRUE);
        assertThat(selectA).hasSize(1);
        TestA myA = selectA.get(0);
        assertThat(myA.children).hasSize(1);
        TestB myB = myA.children.get(0);
        assertThat(myB.children).hasSize(1);
        List<TestB> selectB = tst.query(TestB.class, TestC.class).select(Condition.TRUE);
        assertThat(selectB).hasSize(1);
        myB = selectB.get(0);
        assertThat(myB.children).hasSize(1);

    }

    @Test
    public void testDelete() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel tst = service.getDataModel("TST").get();

        TestA a = tst.getInstance(TestA.class);
        a.init("A");
        TestB b = tst.getInstance(TestB.class);
        b.init(a, "B");
        TestC c = tst.getInstance(TestC.class);
        c.init(b, "C");

        b.addC(c);
        a.addB(b);

        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));

        List<TestA> select = tst.query(TestA.class, TestB.class, TestC.class).select(Condition.TRUE);
        assertThat(select).hasSize(1);
        TestA myA = select.get(0);
        injector.getInstance(TransactionService.class).run(() -> tst.remove(myA));
        assertThat(tst.query(TestA.class, TestB.class, TestC.class).select(Condition.TRUE)).isEmpty();
        assertThat(tst.query(TestB.class, TestC.class).select(Condition.TRUE)).isEmpty();
        assertThat(tst.query(TestC.class).select(Condition.TRUE)).isEmpty();
    }

    @Test
    public void testDeleteChild() {
        OrmService service = injector.getInstance(OrmService.class);
        DataModel tst = service.getDataModel("TST").get();

        TestA a = tst.getInstance(TestA.class);
        a.init("A");
        TestB b = tst.getInstance(TestB.class);
        b.init(a, "B");
        TestC c = tst.getInstance(TestC.class);
        c.init(b, "C");

        b.addC(c);
        a.addB(b);

        injector.getInstance(TransactionService.class).run(() -> tst.persist(a));

        List<TestA> select = tst.query(TestA.class, TestB.class, TestC.class).select(Condition.TRUE);
        assertThat(select).hasSize(1);
        TestA myA = select.get(0);
        assertThat(myA.children).hasSize(1);
        TestB myB = myA.children.get(0);

        injector.getInstance(TransactionService.class).run(() -> myA.remove(myB));
        List<TestA> selectA = tst.query(TestA.class, TestB.class, TestC.class).select(Condition.TRUE);
        assertThat(selectA).hasSize(1);
        assertThat(selectA.get(0).children).isEmpty();
        assertThat(tst.query(TestB.class, TestC.class).select(Condition.TRUE)).isEmpty();
        assertThat(tst.query(TestC.class).select(Condition.TRUE)).isEmpty();
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
        TEST_B {
            @Override
            public void addTo(DataModel dataModel) {
                Table<TestB> testBTable = dataModel.addTable(name(), TestB.class);
                testBTable.map(TestB.class);
                Column idColumn = testBTable.addAutoIdColumn();
                testBTable.column("NAME").varChar(80).notNull().map("name").add();
                Column testa = testBTable.column("TESTA").number().notNull().add();
                testBTable.primaryKey("TEST_B_PK_CONSTRAINT").on(idColumn).add();
                testBTable.foreignKey("TEST_B_FK_TEST_A").on(testa).references(TestA.class).map("parent").reverseMap("children").composition().add();
            }
        },
        TEST_C {
            @Override
            public void addTo(DataModel dataModel) {
                Table<TestC> testCTable = dataModel.addTable(name(), TestC.class);
                testCTable.map(TestC.class);
                Column idColumn = testCTable.addAutoIdColumn();
                testCTable.column("NAME").varChar(80).notNull().map("name").add();
                Column testb = testCTable.column("TESTB").number().notNull().add();
                testCTable.primaryKey("TEST_C_PK_CONSTRAINT").on(idColumn).add();
                testCTable.foreignKey("TEST_C_FK_TEST_B").on(testb).references(TestB.class).map("parent").reverseMap("children").composition().add();
            }
        };;

        public abstract void addTo(DataModel dataModel);
    }

    private static class TestC {
        private long id;
        private String name;
        private Reference<TestB> parent = Reference.empty();

        public void init(TestB parent, String name) {
            this.name = name;
            this.parent.set(parent);
        }
    }

    private static class TestB {
        private long id;
        private String name;
        private Reference<TestA> parent = Reference.empty();
        private List<TestC> children = new ArrayList<>();

        public void init(TestA parent, String name) {
            this.name = name;
            this.parent.set(parent);
        }

        public void addC(TestC c) {
            children.add(c);
        }
    }

    private static class TestA {
        private long id;
        private String name;
        private List<TestB> children = new ArrayList<>();


        public void init(String name) {
            this.name = name;
        }

        public void addB(TestB b) {
            children.add(b);
        }

        public boolean remove(TestB myB) {
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

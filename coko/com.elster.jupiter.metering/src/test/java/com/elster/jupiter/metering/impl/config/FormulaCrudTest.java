package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FormulaBuilder;
import com.elster.jupiter.metering.config.FormulaPart;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.NodeBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FormulaCrudTest {

    private static MetrologyInMemoryBootstrapModule inMemoryBootstrapModule = new MetrologyInMemoryBootstrapModule();

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private MetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }


    @Test
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();

            FormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

            NodeBuilder nodeBuilder = builder.constant(10);
            Formula formula = builder.init(nodeBuilder).build();
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            ConstantNode node = (ConstantNode) nodeBuilder.create();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(ConstantNode.class);
            ConstantNode constantNode = (ConstantNode) myNode;
            assertThat(constantNode.getValue().equals(BigDecimal.TEN));
        }
    }

    @Test
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();

            FormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

            NodeBuilder nodeBuilder = builder.maximum(
                    builder.constant(10),
                    builder.constant(0));
            Formula formula = builder.init(nodeBuilder).build();

            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            FunctionCallNode node = (FunctionCallNode) nodeBuilder.create();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNode.class);
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNode.class);
            assertThat(child2).isInstanceOf(ConstantNode.class);
            ConstantNode constant1 = (ConstantNode) child1;
            assertThat(constant1.getValue().equals(BigDecimal.TEN));
            ConstantNode constant2 = (ConstantNode) child2;
            assertThat(constant2.getValue().equals(BigDecimal.ZERO));
        }
    }


    @Test
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrud()  {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();

            FormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

            NodeBuilder nodeBuilder = builder.maximum(
                    builder.constant(10),
                    builder.plus(
                            builder.constant(10),
                            builder.constant(0)));
            Formula formula = builder.init(nodeBuilder).build();


            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();

            FunctionCallNode node = (FunctionCallNode) nodeBuilder.create();

            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNode.class);
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNode.class);
            assertThat(child2).isInstanceOf(OperationNode.class);
            ConstantNode constant = (ConstantNode) child1;
            assertThat(constant.getValue().equals(BigDecimal.TEN));
            OperationNode operation = (OperationNode) child2;
            assertThat(operation.getOperator().equals(Operator.PLUS));
        }
    }

    @Test
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();

            FormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
            ExpressionNode node = new ExpressionNodeParser().parse("constant(10)");

            Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(ConstantNode.class);
            ConstantNode constantNode = (ConstantNode) myNode;
            assertThat(constantNode.getValue().equals(BigDecimal.TEN));
        }
    }

    @Test
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();

            FormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
            ExpressionNode node = new ExpressionNodeParser().parse("max(constant(10), constant(0))");

            Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();

            context.commit();
            long formulaId = formula.getId();
            Optional<Formula> loadedFormula = service.findFormula(formulaId);
            assertThat(loadedFormula).isPresent();
            Formula myFormula = loadedFormula.get();
            assertThat(myFormula.getId() == formulaId);
            assertThat(myFormula.getMode().equals(myMode));
            ExpressionNode myNode = ((ServerFormula) myFormula).expressionNode();
            assertThat(myNode.equals(node));
            assertThat(myNode).isInstanceOf(FunctionCallNode.class);
            FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
            assertThat(functionCallNode.getFunction().equals(myFunction));
            List<ExpressionNode> children = functionCallNode.getChildren();
            assertThat(children).hasSize(2);
            ExpressionNode child1 = children.get(0);
            ExpressionNode child2 = children.get(1);
            assertThat(child1).isInstanceOf(ConstantNode.class);
            assertThat(child2).isInstanceOf(ConstantNode.class);
            ConstantNode constant1 = (ConstantNode) child1;
            assertThat(constant1.getValue().equals(BigDecimal.TEN));
            ConstantNode constant2 = (ConstantNode) child2;
            assertThat(constant2.getValue().equals(BigDecimal.ZERO));
        }
    }


    @Test
    public void testParser()  {
        try (TransactionContext context = getTransactionService().getContext()) {
            MetrologyConfigurationService service = getMetrologyConfigurationService();
            String formulaString = "multiply(sum(max(constant(10), constant(0)), constant(5), constant(3)), constant(2))";
            ExpressionNode node = new ExpressionNodeParser().parse(formulaString);
            Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
            context.commit();

        }
    }


}
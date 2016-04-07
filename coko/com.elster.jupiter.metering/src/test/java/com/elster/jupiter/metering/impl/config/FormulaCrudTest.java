package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.ConstantNode;
import com.elster.jupiter.metering.config.ExpressionNode;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.Function;
import com.elster.jupiter.metering.config.FunctionCallNode;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.config.OperationNode;
import com.elster.jupiter.metering.config.Operator;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class FormulaCrudTest {

    @Mock
    private ReadingTypeRequirement readingTypeRequirement1;
    @Mock
    private ReadingTypeRequirement readingTypeRequirement2;
    @Mock
    Thesaurus thesaurus;
    @Mock
    MetrologyConfiguration config;

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    private ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService();
    }

    private TransactionService getTransactionService() {
        return inMemoryBootstrapModule.getTransactionService();
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testRequirementNodeCrud() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();


        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType readingType =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.1.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype");
        assertThat(readingType != null);
        config.newReadingTypeRequirement("Aplus").withReadingType(readingType);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);
        ExpressionNodeBuilder nodeBuilder = builder.requirement(req);
        Formula formula = builder.init(nodeBuilder).build();
        assertThat(formula.getExpressionNode() instanceof ReadingTypeRequirementNode);
        ReadingTypeRequirementNode reqNode = (ReadingTypeRequirementNode) formula.getExpressionNode();
        assertThat(reqNode.getReadingTypeRequirement().equals(req));
    }

    @Test
    // formula = 10 (constant)
    @Transactional
    public void test1LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.constant(10);
        Formula formula = builder.init(nodeBuilder).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();
        ConstantNode node = (ConstantNode) nodeBuilder.create();
        assertThat(myNode.equals(node));
        assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constantNode = (ConstantNode) myNode;
        assertThat(constantNode.getValue().equals(BigDecimal.TEN));
    }

    @Test
    @Transactional
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(myMode);

        ExpressionNodeBuilder nodeBuilder = builder.maximum(
                builder.constant(10),
                builder.constant(0));
        Formula formula = builder.init(nodeBuilder).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();
        FunctionCallNodeImpl node = (FunctionCallNodeImpl) nodeBuilder.create();
        assertThat(myNode.equals(node));
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
        assertThat(functionCallNode.getFunction().equals(myFunction));
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constant1 = (ConstantNode) child1;
        assertThat(constant1.getValue().equals(BigDecimal.TEN));
        ConstantNode constant2 = (ConstantNode) child2;
        assertThat(constant2.getValue().equals(BigDecimal.ZERO));
    }


    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrud() {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.maximum(
                builder.constant(10),
                builder.plus(
                        builder.constant(10),
                        builder.constant(0)));
        Formula formula = builder.init(nodeBuilder).build();


        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();

        FunctionCallNodeImpl node = (FunctionCallNodeImpl) nodeBuilder.create();

        assertThat(myNode.equals(node));
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
        assertThat(functionCallNode.getFunction().equals(myFunction));
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(OperationNodeImpl.class);
        ConstantNode constant = (ConstantNode) child1;
        assertThat(constant.getValue().equals(BigDecimal.TEN));
        OperationNodeImpl operation = (OperationNodeImpl) child2;
        assertThat(operation.getOperator().equals(Operator.PLUS));
    }

    @Test
    @Transactional
    // formula = 10 (constant)
    public void test1LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(thesaurus, service, config, myMode).parse("constant(10)");

        Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode.equals(node));
        assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constantNode = (ConstantNode) myNode;
        assertThat(constantNode.getValue().equals(BigDecimal.TEN));
    }

    @Test
    @Transactional
    // formula = max(10, 0) function call + constants
    public void test2LevelNodeStructureCrudUsingParser() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, myMode).parse("max(constant(10), constant(0))");

        Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode.equals(node));
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNodeImpl functionCallNode = (FunctionCallNodeImpl) myNode;
        assertThat(functionCallNode.getFunction().equals(myFunction));
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constant1 = (ConstantNode) child1;
        assertThat(constant1.getValue().equals(BigDecimal.TEN));
        ConstantNode constant2 = (ConstantNode) child2;
        assertThat(constant2.getValue().equals(BigDecimal.ZERO));
    }

    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test3LevelNodeStructureCrudUsingParser() {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ExpressionNode node = new ExpressionNodeParser(thesaurus, service, config, myMode).parse("max(constant(1), plus(constant(2), constant(3)))");

        Formula formula = service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();

        assertThat(myNode.equals(node));
        assertThat(myNode).isInstanceOf(FunctionCallNodeImpl.class);
        FunctionCallNode functionCallNode = (FunctionCallNode) myNode;
        assertThat(functionCallNode.getFunction().equals(myFunction));
        List<ExpressionNode> children = functionCallNode.getChildren();
        assertThat(children).hasSize(2);
        ExpressionNode child1 = children.get(0);
        ExpressionNode child2 = children.get(1);
        assertThat(child1).isInstanceOf(ConstantNodeImpl.class);
        assertThat(child2).isInstanceOf(OperationNodeImpl.class);
        ConstantNode constant = (ConstantNode) child1;
        assertThat(constant.getValue().equals(BigDecimal.TEN));
        OperationNode operation = (OperationNode) child2;
        assertThat(operation.getOperator().equals(Operator.PLUS));
    }

    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void testNoFunctionsAllowedInAutoMode() {

        Formula.Mode myMode = Formula.Mode.AUTO;
        Function myFunction = Function.MAX;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        try {
            ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, myMode).parse("max(constant(1), plus(constant(2), constant(3)))");

            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(),"Functions are not allowed in auto mode.");
        }
    }

    @Test
    @Transactional
    // formula by using the builder = max(10, plus(10, 0)) function call + operator call + constants
    public void test4LevelNodeStructureCrudUsingParser() {

        Formula.Mode myMode = Formula.Mode.EXPERT;
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        String formulaString = "max(constant(1), min(constant(2), constant(3), constant(4)))";
        ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, myMode).parse("max(constant(1), min(constant(2), constant(3), constant(4)))");

        Formula formula = service.newFormulaBuilder(myMode).init(node).build();

        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(myMode));
        ExpressionNode myNode = myFormula.getExpressionNode();

        assertThat(myNode.equals(node));
        assertThat(myNode.toString().equals(formulaString));
    }

    @Test
    @Transactional
    public void testParser() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        String formulaString = "multiply(sum(max(constant(10), constant(0)), constant(5), constant(3)), constant(2))";
        ExpressionNode node = new ExpressionNodeParser(service.getThesaurus(), service, config, Formula.Mode.EXPERT).parse(formulaString);
        service.newFormulaBuilder(Formula.Mode.EXPERT).init(node).build();
        List<Formula> formulas = service.findFormulas();
        for (Formula f : formulas) {
            System.out.println(f.toString());
        }
    }

    @Test
     @Transactional
     // formula = 10 (constant)
     public void testDelete() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.constant(10);
        Formula formula = builder.init(nodeBuilder).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();
        ((ServerFormula) myFormula).delete();
        loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isEmpty();
    }

    @Test
    @Transactional
    // formula = 10 (constant)
    public void testUpdate() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        ServerFormulaBuilder builder = service.newFormulaBuilder(Formula.Mode.EXPERT);

        ExpressionNodeBuilder nodeBuilder = builder.constant(10);
        Formula formula = builder.init(nodeBuilder).build();
        long formulaId = formula.getId();
        Optional<Formula> loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        Formula myFormula = loadedFormula.get();

        ExpressionNode newExpression = builder.constant(99).create();
        myFormula.updateExpression(newExpression);

        loadedFormula = service.findFormula(formulaId);
        assertThat(loadedFormula).isPresent();
        myFormula = loadedFormula.get();
        assertThat(myFormula.getId() == formulaId);
        assertThat(myFormula.getMode().equals(Formula.Mode.EXPERT));
        ExpressionNode myNode = myFormula.getExpressionNode();
        assertThat(myNode.equals(newExpression));
        assertThat(myNode).isInstanceOf(ConstantNodeImpl.class);
        ConstantNode constantNode = (ConstantNode) myNode;
        assertThat(constantNode.getValue().equals(new BigDecimal(99)));
    }

    @Test
    @Transactional
    public void testDeliverableCrud() {
        Formula.Mode myMode = Formula.Mode.EXPERT;
        String name = "deliverable";
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();

        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test5", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType readingType = inMemoryBootstrapModule.getMeteringService().createReadingType("0.0.3.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "test");
        assertThat(readingType != null);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable(name, readingType, myMode);

        ReadingTypeDeliverable deliverable = builder.build(builder.maximum(builder.constant(10), builder.constant(20)));
        assertThat(deliverable.getFormula().getExpressionNode().toString().equals("max(constant(10), constant(20))"));
    }


    @Test
    @Transactional
    // formula = Requirement
    public void createDeliverableWithRequirementThatIsOnADifferentMetrologyConfig() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();


        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType readingType =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "test");
        assertThat(readingType != null);
        config.newReadingTypeRequirement("Aplus").withReadingType(readingType);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test2", serviceCategory.get());
        MetrologyConfiguration config2 = metrologyConfigurationBuilder.create();

        ReadingTypeDeliverableBuilder builder =
                config2.newReadingTypeDeliverable("deliverable", readingType, Formula.Mode.AUTO);

        try {
            builder.build(builder.requirement(req));
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "The requirement with id '" + req.getId() + "' cannot be used because it has a different metrology configuration.");
        }


    }


    @Test
    @Transactional
    // formula = Requirement
    public void createDeliverableOnARequirementThatIsDimensionless() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();


        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType readingTypeRequirement =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.1.12.0.41.109.0.0.0.0.0.0.0.0.0.109.0", "readingtype for requirement");
        assertThat(readingTypeRequirement != null);
        config.newReadingTypeRequirement("consumption").withReadingType(readingTypeRequirement);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        ReadingType readingTypeDeliverable =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype for deliverable");
        assertThat(readingTypeDeliverable != null);

        ReadingTypeDeliverableBuilder builder =
                config.newReadingTypeDeliverable("deliverable", readingTypeDeliverable, Formula.Mode.AUTO);

        try {
            builder.build(builder.requirement(req));
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }


    }

    @Test
    @Transactional
    // formula = Requirement
    public void createDeliverableOnARequirementWithIncompatibleReadingType() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();


        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("test4", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType readingTypeRequirement =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "readingtype for requirement2");
        assertThat(readingTypeRequirement != null);
        config.newReadingTypeRequirement("cons").withReadingType(readingTypeRequirement);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        ReadingType readingTypeDeliverable =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "readingtype for deliverable2");
        assertThat(readingTypeDeliverable != null);

        ReadingTypeDeliverableBuilder builder =
                config.newReadingTypeDeliverable("deliverable", readingTypeDeliverable, Formula.Mode.AUTO);

        try {
            builder.build(builder.requirement(req));
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "The readingtype is not compatible with the dimension of the formula.");
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");
        assertThat(conskWhRT15min != null);
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.requirement(req));

        ReadingType temperatureRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp");
        try {
            deliverable1.setReadingType(temperatureRT);
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "The new readingtype is not compatible with the dimension of the formula(s).");
        }

        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT");

        try {
            deliverable1.setReadingType(conskWhMonthlyRT);
            deliverable1.update();
            assertEquals(deliverable1.getReadingType(), conskWhMonthlyRT);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }

    }

    @Test
    @Transactional
    // formula = Requirement
    public void testUpdateReadingTypeOfDeliverableThatIsusedinAnotherDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType conskWhRT15min =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "conskWh");
        assertThat(conskWhRT15min != null);
        config.newReadingTypeRequirement("Req1").withReadingType(conskWhRT15min);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del1", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable1 = builder.build(builder.requirement(req));

        ReadingTypeDeliverableBuilder builder2 = config.newReadingTypeDeliverable("Del2", conskWhRT15min, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable2 = builder2.build(builder2.deliverable(deliverable1));

        ReadingType temperatureRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "11.2.0.0.0.7.46.0.0.0.0.0.0.0.0.0.23.0", "temp");
        try {
            deliverable1.setReadingType(temperatureRT);
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "The new readingtype is not compatible with the dimension of the formula(s).");
        }

        ReadingType conskWhMonthlyRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "13.0.0.4.4.2.12.0.0.0.0.0.0.0.0.3.72.0", "conskWhMonthlyRT");

        try {
            deliverable1.setReadingType(conskWhMonthlyRT);
            deliverable1.update();
            assertEquals(deliverable1.getReadingType(), conskWhMonthlyRT);
        } catch (InvalidNodeException e) {
            fail("No InvalidNodeException expected!");
        }

    }

    @Test
    @Transactional
    // formula = Requirement
    public void testIrregularReadingTypeOfDeliverable() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config3", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0", "regRT");
        assertThat(regRT != null);

        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("DelOnReg", regRT, Formula.Mode.AUTO);

        try {
            ReadingTypeDeliverable deliverable1 = builder.build(builder.constant(10));
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Irregular readingtypes are not allowed for a deliverable.");
        }
    }

    @Test
    @Transactional
    // formula = Requirement
    public void testIrregularReadingTypeOfRequirement() {
        ServerMetrologyConfigurationService service = getMetrologyConfigurationService();
        Optional<ServiceCategory> serviceCategory =
                inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY);
        assertThat(serviceCategory.isPresent());
        MetrologyConfigurationBuilder metrologyConfigurationBuilder =
                service.newMetrologyConfiguration("config2", serviceCategory.get());
        MetrologyConfiguration config = metrologyConfigurationBuilder.create();
        assertThat(config != null);
        ReadingType regRT =
                inMemoryBootstrapModule.getMeteringService().createReadingType(
                        "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0", "regRT");
        assertThat(regRT != null);
        config.newReadingTypeRequirement("Req2").withReadingType(regRT);

        assertThat(config.getRequirements().size() == 1);
        ReadingTypeRequirement req = service.findReadingTypeRequirement(
                config.getRequirements().get(0).getId()).get();


        ReadingTypeDeliverableBuilder builder = config.newReadingTypeDeliverable("Del3", regRT, Formula.Mode.AUTO);
        try {
            ReadingTypeDeliverable deliverable = builder.build(builder.requirement(req));
            fail("InvalidNodeException expected");
        } catch (InvalidNodeException e) {
            assertEquals(e.getMessage(), "Irregular readingtypes are not allowed for a requirement.");
        }
    }
}
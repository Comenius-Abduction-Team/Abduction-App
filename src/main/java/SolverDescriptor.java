import abduction_api.abducible.*;
import abduction_api.exception.InvalidSolverParameterException;
import abduction_api.exception.NotSupportedException;
import abduction_api.factory.AbductionFactory;
import abduction_api.manager.AbductionManager;
import abduction_api.manager.MultiObservationManager;
import abduction_api.manager.ThreadAbductionManager;
import api_implementation.MhsMxpAbductionFactory;

import java.util.EnumMap;
import java.util.Map;

public class SolverDescriptor {

    public static final Map<Solver, AbductionFactory> solverFactories = new EnumMap<Solver, AbductionFactory>(Solver.class){
        {
            put(Solver.MHS_MXP, MhsMxpAbductionFactory.getFactory());
            put(Solver.LETHE, LetheAbductionFactory.getFactory());
        }
    };

    boolean hasMultiThread, hasMultiObservation, hasSymbolAbducibles = false, hasAxiomAbducibles = false,
            hasConceptSwitch = true,
            hasComplexConceptSwitch = true,
            hasConceptComplementSwitch = true,
            hasRoleSwitch = true,
            hasLoopSwitch = true,
            defaultConceptSwitch,
            defaultComplexConceptSwitch,
            defaultConceptComplementSwitch,
            defaultRoleSwitch,
            defaultLoopSwitch,
            hasSpecificParameters = true, hasTimeLimit = true;

    public boolean hasAbducibles(){
        return hasSymbolAbducibles || hasAxiomAbducibles;
    }

    public boolean hasExplanationConfiguration(){
        return hasConceptSwitch || hasComplexConceptSwitch || hasConceptComplementSwitch || hasRoleSwitch || hasLoopSwitch;
    }

    SolverDescriptor(Solver solver){
        build(solverFactories.get(solver));
    }

    private void build(AbductionFactory factory){

        AbductionManager manager = factory.getAbductionManager();
        AbducibleContainer abducibles;

        hasMultiThread = manager instanceof ThreadAbductionManager;
        hasMultiObservation = manager instanceof MultiObservationManager;

        try{
            abducibles = factory.getSymbolAbducibleContainer();
            hasSymbolAbducibles = true;
        } catch(NotSupportedException ignored){}

        try{
            abducibles = factory.getAxiomAbducibleContainer();
            hasAxiomAbducibles = true;
        } catch(NotSupportedException ignored){}

        try{
            ConceptExplanationConfigurator configurator = factory.getConceptExplanationConfigurator();
            configurator.allowConceptAssertions(true);
            defaultConceptSwitch = configurator.getDefaultConceptAssertionsAllowed();
        } catch(NotSupportedException e){
            hasConceptSwitch = false;
        }

        try{
            ComplexConceptExplanationConfigurator configurator = factory.getComplexConceptExplanationConfigurator();

            try{
                configurator.allowComplexConcepts(true);
                //defaultConfigurator.put(ConfiguratorType.COMPLEX_CONCEPT, configurator.getDefaultComplexConceptsAllowed());
                defaultComplexConceptSwitch = configurator.getDefaultComplexConceptsAllowed();
            } catch (NotSupportedException e) {
                hasComplexConceptSwitch = false;
            }

            try{
                configurator.allowConceptComplements(true);
                defaultConceptComplementSwitch = configurator.getDefaultConceptComplementsAllowed();
            } catch (NotSupportedException e) {
                hasConceptComplementSwitch = false;
            }

        } catch(NotSupportedException e){
            hasComplexConceptSwitch = false;
            hasConceptComplementSwitch = false;
        }

        try{
            RoleExplanationConfigurator configurator = factory.getRoleExplanationConfigurator();

            try{
                configurator.allowRoleAssertions(true);
                defaultRoleSwitch = configurator.getDefaultRoleAssertionsAllowed();
            } catch (NotSupportedException e) {
                hasRoleSwitch = false;
            }

            try{
                configurator.allowLoops(true);
                defaultLoopSwitch = configurator.getDefaultLoopsAllowed();
            } catch (NotSupportedException e) {
                hasLoopSwitch = false;
            }

        } catch(NotSupportedException e){
            hasRoleSwitch = false;
            hasLoopSwitch = false;
        }

        try{
            manager.setSolverSpecificParameters("");
        } catch(NotSupportedException e){
            hasSpecificParameters = false;
        } catch(InvalidSolverParameterException ignored){}

        try{
            manager.setTimeout(0);
        } catch(NotSupportedException e){
            hasTimeLimit = false;
        }

    }

}

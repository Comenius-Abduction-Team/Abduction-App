package sk.uniba.fmph.dai.abduction_app.descriptors;

import sk.uniba.fmph.dai.abduction_api.abducible.IComplexConceptConfigurator;
import sk.uniba.fmph.dai.abduction_api.abducible.IConceptConfigurator;
import sk.uniba.fmph.dai.abduction_api.abducible.IRoleConfigurator;
import sk.uniba.fmph.dai.abduction_api.exception.NotSupportedException;
import sk.uniba.fmph.dai.abduction_api.factory.IAbductionFactory;
import sk.uniba.fmph.dai.abduction_api.factory.ISolverDescriptor;

/** extends the API solver descriptor with additional app-specific info **/
public class ExtendedSolverDescriptor implements ISolverDescriptor {

    private final ISolverDescriptor original;
    private final IAbductionFactory factory;

    private IRoleConfigurator role = null;
    private IConceptConfigurator concept = null;
    private IComplexConceptConfigurator complex = null;

    public ExtendedSolverDescriptor(IAbductionFactory factory){
        original = factory.getDescriptor();
        this.factory = factory;

        try{
            concept = factory.getConceptConfigurator();
        }catch (NotSupportedException ignored){}

        try{
            role = factory.getRoleConfigurator();
        }catch (NotSupportedException ignored){}

        try{
            complex = factory.getComplexConcepConfigurator();
        }catch (NotSupportedException ignored){}

    }

    public IAbductionFactory getFactory() {
        return factory;
    }

    @Override
    public boolean hasThreadMode() {
        return original.hasThreadMode();
    }

    @Override
    public boolean hasSymbolAbducibles() {
        return original.hasSymbolAbducibles();
    }

    @Override
    public boolean hasAxiomAbducibles() {
        return original.hasAxiomAbducibles();
    }

    @Override
    public boolean hasConceptSwitch() {
        return original.hasConceptSwitch();
    }

    @Override
    public boolean hasComplexConceptSwitch() {
        return original.hasComplexConceptSwitch();
    }

    @Override
    public boolean hasComplementConceptSwitch() {
        return original.hasComplementConceptSwitch();
    }

    @Override
    public boolean hasRoleSwitch() {
        return original.hasRoleSwitch();
    }

    @Override
    public boolean hasLoopSwitch() {
        return original.hasLoopSwitch();
    }

    @Override
    public boolean hasSpecificParameters() {
        return original.hasSpecificParameters();
    }

    @Override
    public boolean hasTimeLimit() {
        return original.hasTimeLimit();
    }

    public boolean defaultConceptSwitch() {
        if (hasConceptSwitch())
            return concept.getDefaultConceptAssertionsAllowed();
        else return false;
    }

    public boolean defaultComplexConceptSwitch() {
        if (hasComplexConceptSwitch())
            return complex.getDefaultComplexConceptsAllowed();
        else return false;
    }

    public boolean defaultComplementConceptSwitch() {
        if (hasComplementConceptSwitch())
            return complex.getDefaultComplementConceptsAllowed();
        else return false;
    }

    public boolean defaultRoleSwitch() {
        if (hasRoleSwitch())
            return role.getDefaultRoleAssertionsAllowed();
        else return false;
    }

    public boolean defaultLoopSwitch() {
        if (hasLoopSwitch())
            return role.getDefaultLoopsAllowed();
        else return false;
    }

    public boolean hasAbducibles() {
        return original.hasSymbolAbducibles() || original.hasAxiomAbducibles();
    }

    public boolean hasExplanationConfiguration() {
        return concept != null || complex != null || role != null;
    }
}

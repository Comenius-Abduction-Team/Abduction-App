package sk.uniba.fmph.dai.abduction_app.descriptors;

import sk.uniba.fmph.dai.cats.api_implementation.CatsAbductionFactory;

import java.util.EnumMap;

/** statically provides a descriptor for each solver **/
public class SolverDescriptorMap {

    final static EnumMap<Solver, ExtendedSolverDescriptor> descriptors = new EnumMap<>(Solver.class);

    public static void createDescriptor(Solver solver){
        switch (solver){
            case CATS:
                descriptors.put(solver, new ExtendedSolverDescriptor(CatsAbductionFactory.getFactory()));
        }
    }

    public static ExtendedSolverDescriptor getDescriptor(Solver solver){
        var el = descriptors.get(solver);
        return descriptors.get(solver);
    }

}

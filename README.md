# DL Abduction App

GUI application which can solve logical abduction problems formulated in DL (description logics).

An abduction problem searches for **explanations** - facts that can be added into a **background knowledge** to make it entail an **observation**. The explanations can be limited so that they consist only of the chosen **abducibles** - either concrete facts or symbols that we allow to appear in facts.

The background knowledge, the ontology and the abducibles are all entered as OWL ontologies, either in the RDF/XML or the Manchester syntax.

In Additional settings, the solver to use can be chosen (there is only one functional solver at the moment), along with various options for the abduction problem.

For each solver, specific parameters can be set as strings, analogously to UNIX command line parameters. These parameters are strongly related to the algorithm that is used in the given solver. They are listed here:

For CATS:

- *d <integer>* -- limits the solving algorithm, so that it terminates before the HS-tree reaches the given level
- *sR <true/false>* -- changes how relevance is treated with multiple axioms in the observation


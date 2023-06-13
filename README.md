# DL Abduction App

GUI application which can solve logical abduction problems formulated in DL (description logics).

It uses uses the [LETHE-Abduction solver](https://lat.inf.tu-dresden.de/~koopmann/LETHE-Abduction/index.html), which is currently not available on Maven nor GitHub. For the application to work, you need to download the [JAR file containing the solver and its API implementation](https://drive.google.com/file/d/1EgelAy94pGTTJ-oyKZM5-bONWsawmh3S/view?usp=sharing), and set it as a project library in your IDE.

The background knowledge, the ontology and the abducibles are all entered as OWL ontologies, either in the RDF/XML or the Manchester syntax.

In Additional settings, the solver to use can be chosen, along with setting various options of the abduction. We recommend using the MHS-MXP solver, as it provides more functionality.

For each solver, specific parameters can be set as strings, analogously to UNIX command line parameters. These parameters are strongly related to the algorithmm that is used in the given solver. They are listed here:

For MHS-MXP:

-d <integer> -- limits the solving algorithm, so that it terminates before the HS-tree reaches the given level
  
-mhs <true/false> -- if true, the solver uses the MHS algorithm instead of MHS-MXP
  
-sR <true/false> -- changes how relevance is treated with multiple axioms in the observation
 

For LETHE-Abduction:

-setCheckEntailment <true/false> -- if false, the solver doesn't check whether the background knowledge entails the observation
  
-optimizeUsingModules <true/false> -- algorithm optimisation


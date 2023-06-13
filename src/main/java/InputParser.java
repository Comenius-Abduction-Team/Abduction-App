import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxOntologyParser;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFXMLParser;

import java.util.HashSet;
import java.util.Set;

public class InputParser {

    private static final RDFXMLParser xmlParser = new RDFXMLParser();
    private static final ManchesterOWLSyntaxOntologyParser manchesterParser = new ManchesterOWLSyntaxOntologyParser();

    public static OWLOntology parseOntology(String ontologyText) throws OWLOntologyCreationException {

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = ontologyManager.createOntology();
        StringDocumentSource source = new StringDocumentSource(ontologyText);
        OWLOntologyLoaderConfiguration configuration = new OWLOntologyLoaderConfiguration();

        try{
            xmlParser.parse(source, ontology, configuration);
        } catch (Exception e){
            manchesterParser.parse(source, ontology, configuration);
        }

        return ontology;

    }

    public static Set<OWLEntity> parseSymbolAbducibles(){
        return new HashSet<>();
    }

    public static Set<OWLAxiom> parseAxiomAbducibles(){
        return new HashSet<>();
    }


}

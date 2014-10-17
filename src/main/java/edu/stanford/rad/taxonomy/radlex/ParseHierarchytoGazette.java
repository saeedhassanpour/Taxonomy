package edu.stanford.rad.taxonomy.radlex;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.SimpleRenderer;


public class ParseHierarchytoGazette {

	public static String normalize(String s)
	{
		return s.split("\"")[1].trim();
	}
	
	public static String getID(OWLNamedIndividual i)
	{
		return i.getIRI().toString().split("#")[1].trim();
	}
	
	public static void getNames(OWLNamedIndividual i, List<OWLDataProperty> dVStrings, OWLOntology o, SimpleRenderer renderer, Set<String> dictionary)
	{
		for(OWLDataProperty dVString : dVStrings)
		{
			for (OWLLiteral lit : EntitySearcher.getDataPropertyValues(i, dVString, o)) 
			{
				//System.out.println(normalize(renderer.render(lit))+","+ getID(i));
				String name = normalize(renderer.render(lit));
				dictionary.add(name);
				dictionary.add(name.toLowerCase());
				dictionary.add(name.toUpperCase());
				dictionary.add(toCamelCase(name));
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLDataFactory df = manager.getOWLDataFactory();
	    SimpleRenderer renderer = new SimpleRenderer();
		OWLOntology o = manager.loadOntologyFromOntologyDocument(new File("src/main/resources/Radlex311.owl"));
	    OWLDataProperty prefNameString = df.getOWLDataProperty(IRI.create("http://www.owl-ontologies.com/RADLEX.owl#Preferred_name"));
	    OWLReasoner reasoner = reasonerFactory.createReasoner(o);
	    
//		http://www.owl-ontologies.com/RADLEX.owl#Preferred_name
//    	http://www.owl-ontologies.com/RADLEX.owl#Synonym
//	    http://www.owl-ontologies.com/RADLEX.owl#Misspelling_of_term
//	    http://www.owl-ontologies.com/RADLEX.owl#UMLS_Term
//		http://www.owl-ontologies.com/RADLEX.owl#SNOMED_Term

	    
		String[] dvs = {
				"http://www.owl-ontologies.com/RADLEX.owl#Preferred_name",
				};
		
	    List<OWLDataProperty> dVStrings = new ArrayList<OWLDataProperty>();
	    Set<String> dictionary = new LinkedHashSet<String>();
	    
	    for(String dv : dvs)
	    {
	    	dVStrings.add(df.getOWLDataProperty(IRI.create(dv)));
	    }
	    
		//Observation: "pathophysiologic finding", "benign finding", "portion of body substance", "object", "imaging observation"
		//Modifier: "Radlex descriptor"
		//Anatomy: "anatomical structure", "immaterial anatomical entity", "anatomical set"
	    //Uncertainty: "certainty descriptor"
	    //Modality: "imaging modality"
	    
	    //Observation Size: "size descriptor"
	    //Image Location: "orientation descriptor", "modality descriptor"
	    
	    String concept = "Uncertainty";
		String[] roots = {"certainty descriptor"};
		
		for (String root : roots) {
			for (OWLNamedIndividual i : o.getIndividualsInSignature()) {
				for (OWLLiteral lit : EntitySearcher.getDataPropertyValues(i,
						prefNameString, o)) {

					if (normalize(renderer.render(lit)).equals(root)) {
						getNames(i, dVStrings, o, renderer, dictionary);

						OWLClass cls = df.getOWLClass(i.getIRI());
						NodeSet<OWLClass> clss = reasoner.getSubClasses(cls,
								false);

						for (Node<OWLClass> subcls : clss) {
							OWLNamedIndividual si = df
									.getOWLNamedIndividual(subcls
											.getRepresentativeElement()
											.getIRI());
							getNames(si, dVStrings, o, renderer, dictionary);
						}
					}

				}
			}
		}

		//System.out.println(dictionary.size());
		PrintWriter pw = new PrintWriter("Gazette/" + concept + ".txt", "UTF-8");
		for (String e : dictionary) {
			pw.println(concept + " " + e);
		}
		pw.close();
	}
	
	static String toCamelCase(String s) {
		String[] parts = s.split("\\s+");
		String camelCaseString = "";
		for (String part : parts) {
			camelCaseString += toProperCase(part) + " ";
		}
		return camelCaseString.trim();
	}

	static String toProperCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

}

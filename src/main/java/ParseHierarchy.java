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
import org.semanticweb.owlapi.model.OWLIndividual;
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

public class ParseHierarchy {

	public static String normalize(String s)
	{
		return s.split("\"")[1].trim();
	}
	
	public static void printNames(OWLNamedIndividual i, List<OWLDataProperty> dVStrings, OWLOntology o, SimpleRenderer renderer, Set<String> dictionary)
	{
		for(OWLDataProperty dVString : dVStrings)
		{
			for (OWLLiteral lit : EntitySearcher.getDataPropertyValues(i, dVString, o)) 
			{
				dictionary.add(normalize(renderer.render(lit)));
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLDataFactory df = manager.getOWLDataFactory();
	    SimpleRenderer renderer = new SimpleRenderer();
	    
		OWLOntology o = manager.loadOntologyFromOntologyDocument(new File("/Users/saeed/Documents/workspace/Radlex/src/main/resources/Radlex311.owl"));
	    OWLDataProperty prefNameString = df.getOWLDataProperty(IRI.create("http://www.owl-ontologies.com/RADLEX.owl#Preferred_name"));
	    OWLReasoner reasoner = reasonerFactory.createReasoner(o);
	    
//		http://www.owl-ontologies.com/RADLEX.owl#Preferred_name
//    	http://www.owl-ontologies.com/RADLEX.owl#Synonym
//	    http://www.owl-ontologies.com/RADLEX.owl#Misspelling_of_term
//	    http://www.owl-ontologies.com/RADLEX.owl#UMLS_Term
//	    http://www.owl-ontologies.com/RADLEX.owl#CMA_Label
//		http://www.owl-ontologies.com/RADLEX.owl#SNOMED_Term
	    
//    	<http://www.owl-ontologies.com/RADLEX.owl#JHU_DTI-81>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Definition>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Talairach>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Non-English_name>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Source>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Freesurfer>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Preferred_Name_for_Obsolete>
//    	<http://www.owl-ontologies.com/RADLEX.owl#JHU_White-Matter_Tractography_Atlas>
//    	<http://www.owl-ontologies.com/RADLEX.owl#Comment>
//    	<http://www.owl-ontologies.com/RADLEX.owl#AAL>

	    
		String[] dvs = {
				"http://www.owl-ontologies.com/RADLEX.owl#Preferred_name",
				"http://www.owl-ontologies.com/RADLEX.owl#Synonym",
				"http://www.owl-ontologies.com/RADLEX.owl#Misspelling_of_term",
				"http://www.owl-ontologies.com/RADLEX.owl#UMLS_Term",
				"http://www.owl-ontologies.com/RADLEX.owl#CMA_Label",
				"http://www.owl-ontologies.com/RADLEX.owl#SNOMED_Term",
				};
		
	    List<OWLDataProperty> dVStrings = new ArrayList<OWLDataProperty>();
	    Set<String> dictionary = new LinkedHashSet<String>();
	    
	    for(String dv : dvs)
	    {
	    	dVStrings.add(df.getOWLDataProperty(IRI.create(dv)));
	    }
	    
		//pathophysiologic finding, benign finding, portion of body substance, object, imaging observation
		//Radlex descriptor
		//anatomical structure, immaterial anatomical entity, anatomical set
	    //size descriptor
	    //certainty descriptor
	    //orientation descriptor, modality descriptor
	    //imaging modality
		String root = "imaging modality";

		for (OWLNamedIndividual i : o.getIndividualsInSignature()) {
			for (OWLLiteral lit : EntitySearcher.getDataPropertyValues(i, prefNameString, o)) {

				if (normalize(renderer.render(lit)).equals(root)) 
				{
					printNames(i, dVStrings, o, renderer, dictionary);

					OWLClass cls = df.getOWLClass(i.getIRI());
					NodeSet<OWLClass> clss = reasoner.getSubClasses(cls, false);

					for (Node<OWLClass> subcls : clss) 
					{
						OWLNamedIndividual si = df.getOWLNamedIndividual(subcls.getRepresentativeElement().getIRI());
						printNames(si, dVStrings, o, renderer, dictionary);
					}
				}

			}
		}

		PrintWriter pw = new PrintWriter(root + ".txt", "UTF-8");
		for (String e : dictionary) {
			pw.println(e);
		}
		pw.close();
	}
	
	public void helperMethod(OWLOntology o) {
		for (OWLClass cls : o.getClassesInSignature()) {
			System.out.println(cls);
		}

		for (OWLDataProperty dp : o.getDataPropertiesInSignature()) {
			System.out.println(dp);
		}

		for (OWLNamedIndividual ni : o.getIndividualsInSignature()) {
			System.out.println(ni);

		}

		for (OWLClass cls : o.getClassesInSignature()) {
			for (OWLIndividual indv : cls.getIndividualsInSignature()) {
				System.out.println(indv);
				for (OWLDataProperty dv : indv.getDataPropertiesInSignature()) {
					System.out.println(dv);
				}
			}
		}
	}

}

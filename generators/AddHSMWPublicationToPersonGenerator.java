/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;



import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.XSD;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.dao.jena.QueryUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;


import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.AutocompleteRequiredInputValidator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.PersonHasPublicationValidator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.DateTimeWithPrecisionVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ConstantFieldOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators.AntiXssValidation;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.FrontEndEditingUtils.EditMode;
import edu.cornell.mannlib.vitro.webapp.utils.generators.EditModeUtils;


/**
 * On an add/new, this will show a form, on an edit/update this will skip to the
 * profile page of the publication.
 */
public class AddHSMWPublicationToPersonGenerator extends VivoBaseGenerator implements EditConfigurationGenerator {
    
    public static Log log = LogFactory.getLog(AddPublicationToPersonGenerator.class);


    //* final static String collectionClass = bibo + "Journal"; */
    final static String collectionClass = "http://kerndatensatz-forschung.de/owl/Basis#Sammelband";
    final static String bookClass = bibo + "Book";
    final static String documentClass = "http://purl.obolibrary.org/obo/IAO_0000030";
    final static String conferenceClass = bibo + "Conference";
    final static String editorClass = foaf + "Person";
    final static String publisherClass = vivoCore + "Publisher";
    final static String presentedAtPred = bibo + "presentedAt";
    final static String localePred = vivoCore + "placeOfPublication";
    final static String volumePred = bibo + "volume";
    final static String numberPred = bibo + "edition";
    final static String isbn13Pred = bibo + "isbn13";
    final static String issnPred = bibo + "issn";
    final static String issuePred = bibo + "issue";
    final static String chapterNbrPred = bibo + "chapter";
    final static String startPagePred = bibo + "pageStart";
    final static String endPagePred = bibo + "pageEnd";
    final static String dateTimePred = vivoCore + "dateTimeValue";
    final static String dateTimeValueType = vivoCore + "DateTimeValue";
    final static String dateTimeValue = vivoCore + "dateTime";
    final static String dateTimePrecision = vivoCore + "dateTimePrecision";
    final static String relatesPred = vivoCore + "relates";

    public AddHSMWPublicationToPersonGenerator() {}

	@Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) throws Exception {

     if( EditConfigurationUtils.getObjectUri(vreq) == null ){
         return doAddNew(vreq,session);
     }else{
         return doSkipToPublication(vreq);
     }
    }

    private EditConfigurationVTwo doSkipToPublication(VitroRequest vreq) {
        Individual authorshipNode = EditConfigurationUtils.getObjectIndividual(vreq);

        //try to get the publication
        String pubQueryStr = "SELECT ?obj \n" +
                             "WHERE { <" + authorshipNode.getURI() + "> <" + relatesPred + "> ?obj . \n" +
                             "    ?obj a <" + documentClass + "> . } \n";
        Query pubQuery = QueryFactory.create(pubQueryStr);
        QueryExecution qe = QueryExecutionFactory.create(pubQuery, ModelAccess.on(vreq).getOntModel());
        try {
            ResultSetMem rs = new ResultSetMem(qe.execSelect());
            if(!rs.hasNext()){
                return doBadAuthorshipNoPub( vreq );
            }else if( rs.size() > 1 ){
                return doBadAuthorshipMultiplePubs(vreq);
            }else{
                //skip to publication
                RDFNode objNode = rs.next().get("obj");
                if (!objNode.isResource() || objNode.isAnon()) {
                    return doBadAuthorshipNoPub( vreq );
                }
                EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
                editConfiguration.setSkipToUrl(UrlBuilder.getIndividualProfileUrl(((Resource) objNode).getURI(), vreq));
                return editConfiguration;
            }
        } finally {
            qe.close();
        }
    }


/*

        Adopt from this

        private String getUrlPatternToReturnTo(VitroRequest vreq) {
                String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
                String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
                //Also add domain and range uris if they exist to enable cancel to work properly
                String domainUri = (String) vreq.getParameter("domainUri");
                String rangeUri = (String) vreq.getParameter("rangeUri");
                String generatorName = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.ManageWebpagesForIndividualGenerator";
                String editUrl = EditConfigurationUtils.getEditUrlWithoutContext(vreq);
                String returnPath =  editUrl + "?subjectUri=" + UrlBuilder.urlEncode(subjectUri) +
                "&predicateUri=" + UrlBuilder.urlEncode(predicateUri) +
                "&editForm=" + UrlBuilder.urlEncode(generatorName);
                if(domainUri != null && !domainUri.isEmpty()) {
                        returnPath += "&domainUri=" + UrlBuilder.urlEncode(domainUri);
                }
                if(rangeUri != null && !rangeUri.isEmpty()) {
                        returnPath += "&rangeUri=" + UrlBuilder.urlEncode(rangeUri);
                }
                return returnPath;

        }

*/

    protected EditConfigurationVTwo doAddNew(VitroRequest vreq,
            HttpSession session) throws Exception {
        EditConfigurationVTwo editConfiguration = new EditConfigurationVTwo();
        initBasics(editConfiguration, vreq);
        initPropertyParameters(vreq, session, editConfiguration);
        initObjectPropForm(editConfiguration, vreq);
        setVarNames(editConfiguration);

        // Overriding URL to return to
        // editConfiguration.setUrlPatternToReturnTo(EditConfigurationUtils.getFormUrlWithoutContext(vreq));
        
        // !!! TEST !!! - hard-wired meine Publikation hinterlegt - !!! TEST !!!
        
        String subjectUri = "https://vivo.hs-mittweida.de/vivo/individual/n62803";
        String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
        //Also add domain and range uris if they exist to enable cancel to work properly
        String domainUri = (String) vreq.getParameter("domainUri");
        String rangeUri = (String) vreq.getParameter("rangeUri");
        String generatorName = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.AddPublicationToPersonGenerator";
        String editUrl = EditConfigurationUtils.getEditUrlWithoutContext(vreq);
        String returnPath =  editUrl + "?subjectUri=" + UrlBuilder.urlEncode(subjectUri) +
        "&predicateUri=" + UrlBuilder.urlEncode(predicateUri) +
        "&editForm=" + UrlBuilder.urlEncode(generatorName);
        if(domainUri != null && !domainUri.isEmpty()) {
            returnPath += "&domainUri=" + UrlBuilder.urlEncode(domainUri);
        }
        if(rangeUri != null && !rangeUri.isEmpty()) {
            returnPath += "&rangeUri=" + UrlBuilder.urlEncode(rangeUri);
        }
        log.error("HSMW Error: " + returnPath);
        editConfiguration.setUrlPatternToReturnTo(returnPath);

        // editConfiguration.setEntityToReturnTo(" ?authorshipUri ");

        // Required N3
        editConfiguration.setN3Required(generateN3Required());

        // Optional N3
        editConfiguration.setN3Optional(generateN3Optional());

        editConfiguration.setNewResources(generateNewResources(vreq));

        // In scope
        setUrisAndLiteralsInScope(editConfiguration, vreq);

        // on Form
        setUrisAndLiteralsOnForm(editConfiguration, vreq);

        // Sparql queries
        log.error("HSMWPublictaionToPerson: Adding Queries");
        editConfiguration.addSparqlForAdditionalLiteralsInScope("title", publicationTitleQuery);
        editConfiguration.addSparqlForAdditionalUrisInScope("publicationType", publicationTypeQuery);
        editConfiguration.addSparqlForAdditionalUrisInScope("publicationVenueURI", publicationPublishedInURIQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationVenueLabel", publicationPublishedInLabelQuery);
        editConfiguration.addSparqlForAdditionalUrisInScope("publicationPresentedAtURI", publicationPresentedAtURIQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationPresentedAtLabel", publicationPresentedAtLabelQuery);
        editConfiguration.addSparqlForAdditionalUrisInScope("publicationPublisherURI", publicationPublisherURIQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationPublisherLabel", publicationPublisherLabelQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationLocation", publicationLocationQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationIssue", publicationIssueQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationNumber", publicationNumberQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationVolume", publicationVolumeQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationISBN", publicationISBNQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationISSN", publicationISSNQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationStartPage", publicationStartPageQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationEndPage", publicationEndPageQuery);
        editConfiguration.addSparqlForAdditionalLiteralsInScope("publicationYear", publicationYearQuery);

        // set fields
        setFields(editConfiguration, vreq);

        // template file
        editConfiguration.setTemplate("addHsmwPublicationToPerson.ftl");
        // adding person has publication validator
        editConfiguration.addValidator(new AntiXssValidation());
        editConfiguration.addValidator(new AutocompleteRequiredInputValidator("pubUri", "title"));
        // editConfiguration.addValidator(new PersonHasPublicationValidator());

        // Adding additional data, specifically edit mode
        addFormSpecificData(editConfiguration, vreq);
        prepare(vreq, editConfiguration);
        return editConfiguration;
    }

    private EditConfigurationVTwo doBadAuthorshipMultiplePubs(VitroRequest vreq) {
        // TODO Auto-generated method stub
        return null;
    }

    private EditConfigurationVTwo doBadAuthorshipNoPub(VitroRequest vreq) {
        // TODO Auto-generated method stub
        return null;
    }

    private void setVarNames(EditConfigurationVTwo editConfiguration) {
        editConfiguration.setVarNameForSubject("subject");
        editConfiguration.setVarNameForPredicate("predicate");
        editConfiguration.setVarNameForObject("existingAuthorship");

    }

    /***N3 strings both required and optional***/
    private List<String> generateN3Optional() {
        return list(getN3ForNewPub(),
                    getN3ForNewBookNewPub(),
                    getN3ForNewBook(),
                    getN3ForBook(),
                    getN3ForBookNewPub(),
                    getN3ForNewConferenceNewPub(),
                    getN3ForConferenceNewPub(),
                    getN3ForNewPubNewCoauthor(),
                    getN3ForNewPubCoauthor(),
                    getN3ForPubNewCoauthor(),
                    getN3ForPubCoauthor(),
                    getN3ForNewPubNewEditor(),
                    getN3ForNewPubEditor(),
                    getN3ForPubNewEditor(),
                    getN3ForPubEditor(),
                    getN3ForNewPublisherNewPub(),
                    getN3ForPublisher(),
                    getN3ForNewPublisher(),
                    getN3ForPublisherNewPub(),
                    getN3ForLocaleAssertion(),
                    getN3ForDateTimeAssertion(),
                    getN3ForVolumeAssertion(),
                    getN3ForNumberAssertion(),
                    getN3ForIssueAssertion(),
                    getN3ForISBNAssertion(),
                    getN3ForISSNAssertion(),
                    getN3ForStartPageAssertion(),
                    getN3ForEndPageAssertion()
                    /*,
                    getN3ForExistingPub(),
                    getN3ForNewCollection(),
                    getN3ForNewEvent(),
                    // getN3ForNewCoAuthor0(),
                    // getN3ForNewEditor(),
                    getN3ForNewCollectionNewPub(),
                    getN3ForNewEventNewPub(),
                    // getN3ForNewEditorNewPub(),
                    getN3ForCollection(),
                    getN3ForConference(),
                    getN3ForEvent(),
                    // getN3ForCoAuthor0(),
                    // getN3ForEditor(),
                    getN3ForCollectionNewPub(),
                    getN3ForBookNewPub(),
                    getN3ForEventNewPub(),
                    // getN3ForEditorNewPub(),
                    // additional Editorships
                    // getN3ForNewEditor1(),
                    // getN3ForNewEditor1NewPub(),
                    // getN3ForEditor1(),
                    // getN3ForEditor1NewPub(),
                    // getN3ForNewEditor2(),
                    // getN3ForNewEditor2NewPub(),
                    // getN3ForEditor2(),
                    // getN3ForEditor2NewPub(),
                    getN3FirstNameAssertion(),
                    getN3LastNameAssertion(),
                    getN3ForVolumeAssertion(),
                    getN3ForNumberAssertion(),
                    getN3ForISBN13Assertion(),
                    getN3ForISSNAssertion(),
                    getN3ForIssueAssertion(),
                    getN3ForChapterNbrAssertion(),
                    getN3ForStartPageAssertion(),
                    getN3ForEndPageAssertion(),
                    getN3ForDateTimeAssertion(),
                    // getN3ForNewBookNewEditor(),
                    // getN3ForNewBookEditor(),
                    // getN3ForNewBookNewEditor1(),
                    // getN3ForNewBookEditor1(),
                    // getN3ForNewBookNewEditor2(),
                    // getN3ForNewBookEditor2(),
                    getN3ForNewBookNewPublisher(),
                    getN3ForNewBookPublisher(),
                    getN3ForNewBookVolume(),
                    getN3ForNewBookLocale(),
                    getN3ForNewBookPubDate()
                    // n3s for up to 4 co-authors
                    // getN3ForNewCoAuthor0NewPub(),
                    // getN3ForCoAuthor0NewPub(),
                    // getN3ForNewCoAuthor1NewPub(),
                    // getN3ForCoAuthor1NewPub(),
                    // getN3ForNewCoAuthor2NewPub(),
                    // getN3ForCoAuthor2NewPub(),
                    // getN3ForNewCoAuthor3NewPub(),
                    // getN3ForCoAuthor3NewPub() */
                );
    }

    private List<String> generateN3Required() {
        return list(getAuthorshipN3()
                );
    }


    private String getAuthorshipN3() {
        return "@prefix core: <" + vivoCore + "> . " +
        "?authorshipUri a core:Authorship ;" +
        "core:relates ?subject ." +
        "?authorshipUri <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?subject core:relatedBy ?authorshipUri .";
    }

    private String getN3ForNewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication a ?pubType ." +
        "?newPublication <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newPublication <" + label + "> ?title ." +
        "?authorshipUri core:relates ?newPublication ." +
        "?newPublication core:relatedBy ?authorshipUri .";
    }

    private String getN3ForNewPubNewCoauthor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship ." +
        "?coauthorship a core:Authorship ." +
        "?coauthorship core:relates ?newCoAuthor ." +
        "?newCoAuthor a  <http://xmlns.com/foaf/0.1/Person> ." +
        "?newCoAuthor <" + label + "> ?coauthorlabel ." ;
    }

    private String getN3ForNewPubCoauthor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship ." +
        "?coauthorship a core:Authorship ." +
        "?coauthorship core:relates ?coauthorUri .";
    }

    private String getN3ForPubNewCoauthor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?pubUri core:relatedBy ?coauthorship ." +
        "?coauthorship a core:Authorship ." +
        "?coauthorship core:relates ?newCoAuthor ." +
        "?newCoAuthor a  <http://xmlns.com/foaf/0.1/Person> ." +
        "?newCoAuthor <" + label + "> ?coauthorlabel ." ;
    }

    private String getN3ForPubCoauthor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?pubUri core:relatedBy ?coauthorship ." +
        "?coauthorship a core:Authorship ." +
        "?coauthorship core:relates ?coauthorUri .";
    }

    private String getN3ForNewPubNewEditor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?editorship ." +
        "?editorship a core:Editorship ." +
        "?editorship core:relates ?newEditor ." +
        "?newEditor a  ?editorType ." +
        "?newEditor <" + label + "> ?editorlabel ." ;
    }

    private String getN3ForNewPubEditor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?editorship ." +
        "?editorship a core:Editorship ." +
        "?editorship core:relates ?editorUri .";
    }

    private String getN3ForPubNewEditor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?pubUri core:relatedBy ?editorship ." +
        "?editorship a core:Editorship ." +
        "?editorship core:relates ?newEditor ." +
        "?newEditor a  ?editorType ." +
        "?newEditor <" + label + "> ?editorlabel ." ;
    }

    private String getN3ForPubEditor() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?pubUri core:relatedBy ?editorship ." +
        "?editorship a core:Editorship ." +
        "?editorship core:relates ?editorUri .";
    }


    /*
    private String getN3ForNewCoAuthor0NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship0 ." +
        "?coauthorship0 a core:Authorship ." +
        "?coauthorship0 <http://vivoweb.org/ontology/core#rank> \"2\" ." +
        "?coauthorship0 core:relates ?newCoAuthor0 ." +
        "?newCoAuthor0 a  <http://xmlns.com/foaf/0.1/Person> ." +
        "?newCoAuthor0 <" + label + "> ?co0_label ." ;
    }

    private String getN3ForCoAuthor0NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship0 ." +
        "?coauthorship0 a core:Authorship ." +
        "?coauthorship0 <http://vivoweb.org/ontology/core#rank> \"2\" ." +
        "?coauthorship0 core:relates ?co0Uri .";
    }

    private String getN3ForNewCoAuthor1NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship1 ." +
        "?coauthorship1 a core:Authorship ." +
        "?coauthorship1 <http://vivoweb.org/ontology/core#rank> \"3\" ." +
        "?coauthorship1 core:relates ?newCoAuthor1 ." +
        "?newCoAuthor1 a  <http://xmlns.com/foaf/0.1/Person> ." +
        "?newCoAuthor1 <" + label + "> ?co1_label ." ;
    }

    private String getN3ForCoAuthor1NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship1 ." +
        "?coauthorship1 a core:Authorship ." +
        "?coauthorship1 <http://vivoweb.org/ontology/core#rank> \"3\" ." +    
        "?coauthorship1 core:relates ?co1Uri .";
    }

    private String getN3ForNewCoAuthor2NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship2 ." +
        "?coauthorship2 a core:Authorship ." +
        "?coauthorship2 <http://vivoweb.org/ontology/core#rank> \"4\" ." +  
        "?coauthorship2 core:relates ?newCoAuthor2 ." +
        "?newCoAuthor2 a  <http://xmlns.com/foaf/0.1/Person> ." +
        "?newCoAuthor2 <" + label + "> ?co2_label ." ;
    }

    private String getN3ForCoAuthor2NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship2 ." +
        "?coauthorship2 a core:Authorship ." +
        "?coauthorship2 <http://vivoweb.org/ontology/core#rank> \"4\" ." + 
        "?coauthorship2 core:relates ?co2Uri .";
    }

    private String getN3ForNewCoAuthor3NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship3 ." +
        "?coauthorship3 a core:Authorship ." +
        "?coauthorship3 <http://vivoweb.org/ontology/core#rank> \"5\" ." + 
        "?coauthorship3 core:relates ?newCoAuthor3 ." +
        "?newCoAuthor3 a  <http://xmlns.com/foaf/0.1/Person> ." +
        "?newCoAuthor3 <" + label + "> ?co3_label ." ;
    }

    private String getN3ForCoAuthor3NewPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?newPublication core:relatedBy ?coauthorship3 ." +
        "?coauthorship3 a core:Authorship ." +
        "?coauthorship3 <http://vivoweb.org/ontology/core#rank> \"5\" ." +  
        "?coauthorship3 core:relates ?co3Uri .";
    }
    */


    private String getN3ForExistingPub() {
        return "@prefix core: <" + vivoCore + "> ." +
        "?authorshipUri core:relates ?pubUri ." +
        "?pubUri core:relatedBy ?authorshipUri .";
    }

    private String getN3ForNewCollectionNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:hasPublicationVenue ?newCollection . \n" +
        "?newCollection a <" + collectionClass + "> . \n" +
        "?newCollection <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newCollection vivo:publicationVenueFor ?newPublication . \n" +
        "?newCollection <" + label + "> ?collection .";
    }

    private String getN3ForNewCollection() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:hasPublicationVenue ?newCollection . \n" +
        "?newCollection a <" + collectionClass + ">  . \n" +
        "?newCollection <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newCollection vivo:publicationVenueFor ?pubUri . \n" +
        "?newCollection <" + label + "> ?collection .";
    }

    private String getN3ForCollectionNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:hasPublicationVenue ?collectionUri . \n" +
        "?collectionUri vivo:publicationVenueFor ?newPublication . ";
    }

    private String getN3ForCollection() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:hasPublicationVenue ?collectionUri . \n" +
        "?collectionUri vivo:publicationVenueFor ?pubUri . ";
    }

    private String getN3ForNewBook() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:hasPublicationVenue ?newBook . \n" +
        "?newBook a ?bookType  . \n" +
        "?newBook <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newBook vivo:publicationVenueFor ?pubUri . \n " +
        "?newBook <" + label + "> ?book .";
    }

    private String getN3ForBook() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:hasPublicationVenue ?bookUri . \n" +
        "?bookUri vivo:publicationVenueFor ?pubUri . ";
    }

    private String getN3ForNewBookNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:hasPublicationVenue ?newBook . \n" +
        "?newBook a ?bookType  . \n" +
        "?newBook <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " . \n" +
        "?newBook vivo:publicationVenueFor ?newPublication . \n " +
        "?newBook <" + label + "> ?book . ";
    }

    private String getN3ForNewBookVolume() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook <" + volumePred + "> ?volume . ";
    }

    private String getN3ForNewBookLocale() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook <" + localePred + "> ?locale . ";
    }

    private String getN3ForNewBookPubDate() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook <" + dateTimePred + "> ?dateTimeNode . \n" +
        "?dateTimeNode a <" + dateTimeValueType + "> . \n" +
        "?dateTimeNode <" + dateTimeValue + "> ?dateTime-value . \n" +
        "?dateTimeNode <" + dateTimePrecision + "> ?dateTime-precision .";
    }

    private String getN3ForNewBookNewEditor() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:relatedBy ?editorship0 . \n" +
        "?editorship0 vivo:relates ?newBook . \n" +
        "?newBook <" + label + "> ?book . \n " +
        "?editorship0 a vivo:Editorship . \n" +
        "?editorship0 vivo:relates ?newEditor0 . \n" +
        "?newEditor0 a ?editor0Type  . \n" +
        "?newEditor0 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor0 vivo:relatedBy ?editorship0 . \n" +
        "?newEditor0 <" + label + "> ?editor0 .";
    }

    private String getN3ForNewBookEditor() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:relatedBy ?editorship0 . \n" +
        "?editorship0 vivo:relates ?newBook . \n" +
        "?newBook <" + label + "> ?book . \n " +
        "?editorship0 a vivo:Editorship . \n" +
        "?editorship0 vivo:relates ?editor0Uri . \n" +
        "?editorUri vivo:relatedBy ?editorship0 . ";
    }

    private String getN3ForNewBookNewEditor1() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:relatedBy ?editorship1 . \n" +
        "?editorship1 vivo:relates ?newBook . \n" +
        "?newBook <" + label + "> ?book . \n " +
        "?editorship1 a vivo:Editorship . \n" +
        "?editorship1 vivo:relates ?newEditor1 . \n" +
        "?newEditor1 a ?editor1Type  . \n" +
        "?newEditor1 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor1 vivo:relatedBy ?editorship1 . \n" +
        "?newEditor1 <" + label + "> ?editor1 .";
    }

    private String getN3ForNewBookEditor1() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:relatedBy ?editorship1 . \n" +
        "?editorship1 vivo:relates ?newBook . \n" +
        "?newBook <" + label + "> ?book . \n " +
        "?editorship1 a vivo:Editorship . \n" +
        "?editorship1 vivo:relates ?editor1Uri . \n" +
        "?editorUri1 vivo:relatedBy ?editorship1 . ";
    }

    private String getN3ForNewBookNewEditor2() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:relatedBy ?editorship2 . \n" +
        "?editorship2 vivo:relates ?newBook . \n" +
        "?newBook <" + label + "> ?book . \n " +
        "?editorship2 a vivo:Editorship . \n" +
        "?editorship2 vivo:relates ?newEditor2 . \n" +
        "?newEditor2 a ?editor2Type  . \n" +
        "?newEditor2 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor2 vivo:relatedBy ?editorship2 . \n" +
        "?newEditor2 <" + label + "> ?editor2 .";
    }

    private String getN3ForNewBookEditor2() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:relatedBy ?editorship2 . \n" +
        "?editorship2 vivo:relates ?newBook . \n" +
        "?newBook <" + label + "> ?book . \n " +
        "?editorship2 a vivo:Editorship . \n" +
        "?editorship2 vivo:relates ?editor2Uri . \n" +
        "?editorUri2 vivo:relatedBy ?editorship2 . ";
    }


    private String getN3ForNewBookNewPublisher() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:publisher ?newPublisher . \n " +
        "?newPublisher vivo:publisherOf ?newBook . \n" +
        "?newPublisher <" + label + "> ?publisher .";
     }

    private String getN3ForNewBookPublisher() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newBook vivo:publisher ?publisherUri . \n" +
        "?publisherUri vivo:publisherOf ?newBook . ";
    }

    private String getN3ForBookNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:hasPublicationVenue ?bookUri . \n" +
        "?bookUri vivo:publicationVenueFor ?newPublication . ";
    }

    private String getN3ForNewConference() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri <" + presentedAtPred + "> ?newConference . \n" +
        "?newConference a <" + conferenceClass + ">  . \n" +
        "?newConference <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newConference <http://purl.obolibrary.org/obo/BFO_0000051> ?pubUri . \n" +
        "?newConference <" + label + "> ?conference .";
    }

    private String getN3ForConference() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri <" + presentedAtPred + "> ?conferenceUri . \n" +
        "?conferenceUri <http://purl.obolibrary.org/obo/BFO_0000051> ?pubUri . ";
    }

    private String getN3ForNewConferenceNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + presentedAtPred + "> ?newConference . \n" +
        "?newConference a ?conferenceType  . \n" +
        "?newConference <http://purl.obolibrary.org/obo/BFO_0000051> ?newPublication . \n" +
        "?newConference <" + label + "> ?conferencelabel .";
    }

    private String getN3ForConferenceNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + presentedAtPred + "> ?conferenceUri . \n" +
        "?conferenceUri <http://purl.obolibrary.org/obo/BFO_0000051> ?newPublication . ";
    }

    private String getN3ForNewEvent() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:proceedingsOf ?newEvent . \n" +
        "?newEvent a <" + conferenceClass + ">  . \n" +
        "?newEvent <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEvent vivo:hasProceedings ?pubUri . \n" +
        "?newEvent <" + label + "> ?event .";
    }

    private String getN3ForEvent() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:proceedingsOf ?eventUri . \n" +
        "?eventUri vivo:hasProceedings ?pubUri . ";
    }

    private String getN3ForNewEventNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:proceedingsOf ?newEvent . \n" +
        "?newEvent a <" + conferenceClass + ">  . \n" +
        "?newEvent <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEvent vivo:hasProceedings ?newPublication . \n" +
        "?newEvent <" + label + "> ?event .";
    }

    private String getN3ForEventNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:proceedingsOf ?eventUri . \n" +
        "?eventUri vivo:hasProceedings ?newPublication . ";
    }

    // SPARQL fuer Editor / Publikatikon
    
    /*
    // Editor 1
    private String getN3ForNewEditor() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:relatedBy ?editorship0 . \n" +
        "?editorship0 vivo:relates ?pubUri . \n" +
        "?editorship0 a vivo:Editorship . \n" +
        "?editorship0 vivo:relates ?newEditor0 . \n" +
        "?newEditor0 a ?editor0Type  . \n" +
        "?newEditor0 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor0 vivo:relatedBy ?editorship0 . \n" +
        "?newEditor0 <" + label + "> ?editor0 .";
    }

    private String getN3ForEditor() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:relatedBy ?editorship0 . \n" +
        "?editorship0 vivo:relates ?pubUri . \n" +
        "?editorship0 a vivo:Editorship . \n" +
        "?editorship0 vivo:relates ?editor0Uri . \n" +
        "?editor0Uri vivo:relatedBy ?editorship0 . ";
    }

    private String getN3ForNewEditorNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:relatedBy ?editorship0 . \n" +
        "?editorship0 vivo:relates ?newPublication . \n" +
        "?newPublication <" + label + "> ?title ." +
        "?editorship0 a vivo:Editorship . \n" +
        "?editorship0 vivo:relates ?newEditor0 . \n" +
        "?newEditor0 a ?editor0Type  . \n" +
        "?newEditor0 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor0 vivo:relatedBy ?editorship0 . \n" +
        "?newEditor0 <" + label + "> ?editor0 .";
    }

    private String getN3ForEditorNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:relatedBy ?editorship0 . \n" +
        "?editorship0 vivo:relates ?newPublication . \n" +
        "?newPublication <" + label + "> ?title ." +
        "?editorship0 vivo:relates ?editor0Uri . \n" +
        "?editorship0 a vivo:Editorship . \n" +
        "?editor0Uri vivo:relatedBy ?editorship0 . ";
    }

    // Editor 2
    private String getN3ForNewEditor1() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:relatedBy ?editorship1 . \n" +
        "?editorship1 vivo:relates ?pubUri . \n" +
        "?editorship1 a vivo:Editorship . \n" +
        "?editorship1 vivo:relates ?newEditor1 . \n" +
        "?newEditor1 a ?editor1Type  . \n" +
        "?newEditor1 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor1 vivo:relatedBy ?editorship1 . \n" +
        "?newEditor1 <" + label + "> ?editor1 .";
    }

    private String getN3ForEditor1() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:relatedBy ?editorship1 . \n" +
        "?editorship1 vivo:relates ?pubUri . \n" +
        "?editorship1 a vivo:Editorship . \n" +
        "?editorship1 vivo:relates ?editor1Uri . \n" +
        "?editor1Uri vivo:relatedBy ?editorship1 . ";
    }

    private String getN3ForNewEditor1NewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:relatedBy ?editorship1 . \n" +
        "?editorship1 vivo:relates ?newPublication . \n" +
        "?newPublication <" + label + "> ?title ." +
        "?editorship1 a vivo:Editorship . \n" +
        "?editorship1 vivo:relates ?newEditor1 . \n" +
        "?newEditor1 a ?editor1Type  . \n" +
        "?newEditor1 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor1 vivo:relatedBy ?editorship1 . \n" +
        "?newEditor1 <" + label + "> ?editor1 .";
    }

    private String getN3ForEditor1NewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:relatedBy ?editorship1 . \n" +
        "?editorship1 vivo:relates ?newPublication . \n" +
        "?newPublication <" + label + "> ?title ." +
        "?editorship1 vivo:relates ?editor1Uri . \n" +
        "?editorship1 a vivo:Editorship . \n" +
        "?editor1Uri vivo:relatedBy ?editorship1 . ";
    }

    // Editor 3
    private String getN3ForNewEditor2() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:relatedBy ?editorship2 . \n" +
        "?editorship2 vivo:relates ?pubUri . \n" +
        "?editorship2 a vivo:Editorship . \n" +
        "?editorship2 vivo:relates ?newEditor2 . \n" +
        "?newEditor2 a ?editor2Type  . \n" +
        "?newEditor2 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor2 vivo:relatedBy ?editorship2 . \n" +
        "?newEditor2 <" + label + "> ?editor2 .";
    }

    private String getN3ForEditor2() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:relatedBy ?editorship2 . \n" +
        "?editorship2 vivo:relates ?pubUri . \n" +
        "?editorship2 a vivo:Editorship . \n" +
        "?editorship2 vivo:relates ?editor2Uri . \n" +
        "?editor2Uri vivo:relatedBy ?editorship2 . ";
    }

    private String getN3ForNewEditor2NewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:relatedBy ?editorship2 . \n" +
        "?editorship2 vivo:relates ?newPublication . \n" +
        "?newPublication <" + label + "> ?title ." +
        "?editorship2 a vivo:Editorship . \n" +
        "?editorship2 vivo:relates ?newEditor2 . \n" +
        "?newEditor2 a ?editor2Type  . \n" +
        "?newEditor2 <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newEditor2 vivo:relatedBy ?editorship2 . \n" +
        "?newEditor2 <" + label + "> ?editor2 .";
    }

    private String getN3ForEditor2NewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:relatedBy ?editorship2 . \n" +
        "?editorship2 vivo:relates ?newPublication . \n" +
        "?newPublication <" + label + "> ?title ." +
        "?editorship2 vivo:relates ?editor2Uri . \n" +
        "?editorship2 a vivo:Editorship . \n" +
        "?editor2Uri vivo:relatedBy ?editorship2 . ";
    }
    // Ende Editorships
    */

    private String getN3ForNewPublisher() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:publisher ?newPublisher . \n" +
        "?newPublisher a <" + publisherClass + ">  . \n" +
        "?newPublisher <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newPublisher vivo:publisherOf ?pubUri . \n" +
        "?newPublisher <" + label + "> ?publisher .";
    }

    private String getN3ForPublisher() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?pubUri vivo:publisher ?publisherUri . \n" +
        "?publisherUri vivo:publisherOf ?pubUri . ";
    }

    private String getN3ForNewPublisherNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:publisher ?newPublisher . \n" +
        "?newPublisher a <" + publisherClass + ">  . \n" +
        "?newPublisher <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#createdAt> " + this.getCurrentTime() + " ." +
        "?newPublisher vivo:publisherOf ?newPublication . \n" +
        "?newPublisher <" + label + "> ?publisher .";
    }

    private String getN3ForPublisherNewPub() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication vivo:publisher ?publisherUri . \n" +
        "?publisherUri vivo:publisherOf ?newPublication . ";
    }

    private String getN3FirstNameAssertion() {
        return "@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .  \n" +
        "?newEditor <http://purl.obolibrary.org/obo/ARG_2000028>  ?vcardEditor . \n" +
        "?vcardEditor <http://purl.obolibrary.org/obo/ARG_2000029>  ?newEditor . \n" +
        "?vcardEditor a <http://www.w3.org/2006/vcard/ns#Individual> . \n" +
        "?vcardEditor vcard:hasName  ?vcardName . \n" +
        "?vcardName a <http://www.w3.org/2006/vcard/ns#Name> . \n" +
        "?vcardName vcard:givenName ?firstName .";
    }

    private String getN3LastNameAssertion() {
        return "@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .  \n" +
        "?newEditor <http://purl.obolibrary.org/obo/ARG_2000028>  ?vcardEditor . \n" +
        "?vcardEditor <http://purl.obolibrary.org/obo/ARG_2000029>  ?newEditor . \n" +
        "?vcardEditor a <http://www.w3.org/2006/vcard/ns#Individual> . \n" +
        "?vcardEditor vcard:hasName  ?vcardName . \n" +
        "?vcardName a <http://www.w3.org/2006/vcard/ns#Name> . \n" +
        "?vcardName vcard:familyName ?lastName .";
    }

    private String getN3ForLocaleAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + localePred + "> ?locale .  ";
    }

    private String getN3ForVolumeAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + volumePred + "> ?volume .  ";
    }

    private String getN3ForNumberAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + numberPred + "> ?edition .  ";
    }

    private String getN3ForISBNAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + isbn13Pred + "> ?isbn .  ";
    }

    private String getN3ForISSNAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + issnPred + "> ?issn .  ";
    }

    private String getN3ForIssueAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + issuePred + "> ?issue .  ";
    }

    private String getN3ForChapterNbrAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + chapterNbrPred + "> ?chapterNbr .  ";
    }

    private String getN3ForStartPageAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + startPagePred + "> ?startPage . ";
    }

    private String getN3ForEndPageAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + endPagePred + ">?endPage . ";
    }

    private String getN3ForDateTimeAssertion() {
        return "@prefix vivo: <" + vivoCore + "> . \n" +
        "?newPublication <" + dateTimePred + "> ?dateTimeNode . \n" +
        "?dateTimeNode a <" + dateTimeValueType + "> . \n" +
        "?dateTimeNode <" + dateTimeValue + "> ?dateTime-value . \n" +
        "?dateTimeNode <" + dateTimePrecision + "> ?dateTime-precision . ";
    }

    /**  Get new resources	 */
    private Map<String, String> generateNewResources(VitroRequest vreq) {
        String DEFAULT_NS_TOKEN=null; //null forces the default NS

        HashMap<String, String> newResources = new HashMap<String, String>();
        newResources.put("authorshipUri", DEFAULT_NS_TOKEN);
        newResources.put("newPublication", DEFAULT_NS_TOKEN);
        newResources.put("newCollection", DEFAULT_NS_TOKEN);
        newResources.put("newBook", DEFAULT_NS_TOKEN);
        newResources.put("newConference", DEFAULT_NS_TOKEN);
        newResources.put("newEvent", DEFAULT_NS_TOKEN);
        newResources.put("vcardEditor", DEFAULT_NS_TOKEN);
        newResources.put("vcardName", DEFAULT_NS_TOKEN);
        newResources.put("newPublisher", DEFAULT_NS_TOKEN);
        newResources.put("dateTimeNode", DEFAULT_NS_TOKEN);
        newResources.put("newCoAuthor", DEFAULT_NS_TOKEN);
        newResources.put("coauthorship", DEFAULT_NS_TOKEN);
        newResources.put("newEditor", DEFAULT_NS_TOKEN);
        newResources.put("editorship", DEFAULT_NS_TOKEN);
        return newResources;
    }

    /** Set URIS and Literals In Scope and on form and supporting methods	 */
    private void setUrisAndLiteralsInScope(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
        HashMap<String, List<String>> urisInScope = new HashMap<String, List<String>>();
        urisInScope.put(editConfiguration.getVarNameForSubject(),
                Arrays.asList(new String[]{editConfiguration.getSubjectUri()}));
        urisInScope.put(editConfiguration.getVarNameForPredicate(),
                Arrays.asList(new String[]{editConfiguration.getPredicateUri()}));
        editConfiguration.setUrisInScope(urisInScope);
        HashMap<String, List<Literal>> literalsInScope = new HashMap<String, List<Literal>>();
        editConfiguration.setLiteralsInScope(literalsInScope);

    }

    private void setUrisAndLiteralsOnForm(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
        List<String> urisOnForm = new ArrayList<String>();
        //add role activity and roleActivityType to uris on form
        urisOnForm.add("pubType");
        urisOnForm.add("pubUri");
        urisOnForm.add("collectionUri");
        urisOnForm.add("bookUri");
        urisOnForm.add("bookType");
        urisOnForm.add("conferenceUri");
        urisOnForm.add("conferenceType");
        urisOnForm.add("eventUri");
        urisOnForm.add("editorUri");
        urisOnForm.add("editorType");
        urisOnForm.add("publisherUri");
        // up to 4 existing co-authors
        urisOnForm.add("coauthorUri");
        editConfiguration.setUrisOnform(urisOnForm);

        //activity label and role label are literals on form
        List<String> literalsOnForm = new ArrayList<String>();
        literalsOnForm.add("title");
        literalsOnForm.add("collection");
        literalsOnForm.add("book");
        literalsOnForm.add("conferencelabel");
        literalsOnForm.add("event");
        literalsOnForm.add("editorlabel");
        literalsOnForm.add("publisher");
        literalsOnForm.add("collectionDisplay");
        literalsOnForm.add("bookDisplay");
        literalsOnForm.add("conferenceDisplay");
        literalsOnForm.add("eventDisplay");
        literalsOnForm.add("editorDisplay");
        literalsOnForm.add("publisherDisplay");
        literalsOnForm.add("locale");
        literalsOnForm.add("volume");
        literalsOnForm.add("edition");
        literalsOnForm.add("issue");
        literalsOnForm.add("chapterNbr");
        literalsOnForm.add("startPage");
        literalsOnForm.add("endPage");
        literalsOnForm.add("firstName");
        literalsOnForm.add("lastName");
        literalsOnForm.add("isbn");
        literalsOnForm.add("issn");
        // up to 4 coauthors
        literalsOnForm.add("coauthorlabel");
        editConfiguration.setLiteralsOnForm(literalsOnForm);
    }

    /** Set SPARQL Queries and supporting methods. */
    //In this case no queries for existing
    // private void setSparqlQueries(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {
    //    editConfiguration.setSparqlForExistingUris(new HashMap<String, String>());
    //    editConfiguration.setSparqlForAdditionalLiteralsInScope(new HashMap<String, String>());
    //    editConfiguration.setSparqlForAdditionalUrisInScope(new HashMap<String, String>());
    //}

    /**
     *
     * Set Fields and supporting methods
     * @throws Exception
     */

    private void setFields(EditConfigurationVTwo editConfiguration, VitroRequest vreq) throws Exception {
        setTitleField(editConfiguration);
        setPubTypeField(editConfiguration);
        setPubUriField(editConfiguration);
        setCollectionLabelField(editConfiguration);
        setCollectionDisplayField(editConfiguration);
        setCollectionUriField(editConfiguration);
        setBookLabelField(editConfiguration);
        setBookDisplayField(editConfiguration);
        setBookUriField(editConfiguration);
        setBookTypeField(editConfiguration);
        setConferenceLabelField(editConfiguration);
        setConferenceDisplayField(editConfiguration);
        setConferenceUriField(editConfiguration);
        setEventLabelField(editConfiguration);
        setEventDisplayField(editConfiguration);
        setEventUriField(editConfiguration);
        setEditorLabelField(editConfiguration);
        setEditorDisplayField(editConfiguration);
        setFirstNameField(editConfiguration);
        setLastNameField(editConfiguration);
        setEditorUriField(editConfiguration);
        setPublisherLabelField(editConfiguration);
        setPublisherDisplayField(editConfiguration);
        setPublisherUriField(editConfiguration);
        setLocaleField(editConfiguration);
        setVolumeField(editConfiguration);
        setNumberField(editConfiguration);
        setISSNField(editConfiguration);
        setIssueField(editConfiguration);
        setChapterNbrField(editConfiguration);
        setStartPageField(editConfiguration);
        setEndPageField(editConfiguration);
        setDateTimeField(editConfiguration);
        setCoauthorfields(editConfiguration);
    }

    private void setTitleField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("title").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setPubTypeField(EditConfigurationVTwo editConfiguration) throws Exception {
        editConfiguration.addField(new FieldVTwo().
                setName("pubType").
                setValidators( list("nonempty") ).
                setOptions( new ConstantFieldOptions("pubType", getPublicationTypeLiteralOptions() ))
                );
    }

    private void setPubUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("pubUri"));
    }

    private void setCollectionLabelField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("collection").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setCollectionDisplayField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("collectionDisplay").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setCollectionUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("collectionUri"));
    }

    private void setBookLabelField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("book").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setBookDisplayField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("bookDisplay").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setBookUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("bookUri"));
    }

    private void setBookTypeField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("bookType"));
    }

    private void setConferenceLabelField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("conference").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setConferenceDisplayField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("conferenceDisplay").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setConferenceUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("conferenceUri"));
    }

    private void setEventLabelField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("event").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setEventDisplayField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("eventDisplay").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }


    private void setFirstNameField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("firstName").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setLastNameField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("lastName").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }
            
    private void setEventUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("eventUri"));
    }

    private void setEditorLabelField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("editorlabel").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setEditorDisplayField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("editorDisplay").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setEditorUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("editorUri"));
        editConfiguration.addField(new FieldVTwo().
                setName("editorType"));
      
    }

    private void setPublisherLabelField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("publisher").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setPublisherDisplayField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("publisherDisplay").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setPublisherUriField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("publisherUri"));
    }

    private void setLocaleField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("locale").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setVolumeField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("volume").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setNumberField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("edition").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setISSNField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("isbn").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
        editConfiguration.addField(new FieldVTwo().
                setName("issn").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }


    private void setIssueField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("issue").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setChapterNbrField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("chapterNbr").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setStartPageField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("startPage").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setEndPageField(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
                setName("endPage").
                setValidators(list("datatype:" + stringDatatypeUri)).
                setRangeDatatypeUri(stringDatatypeUri));
    }

    private void setDateTimeField(EditConfigurationVTwo editConfiguration) {
        editConfiguration.addField(new FieldVTwo().
                setName("dateTime").
                setEditElement(
                new DateTimeWithPrecisionVTwo(null,
                        VitroVocabulary.Precision.YEAR.uri(),
                        VitroVocabulary.Precision.NONE.uri())
                        )
                );
    }

    private void setCoauthorfields(EditConfigurationVTwo editConfiguration) {
        String stringDatatypeUri = XSD.xstring.toString();
        editConfiguration.addField(new FieldVTwo().
            setName("coauthorlabel").
            setValidators(list("datatype:" + stringDatatypeUri)).
            setRangeDatatypeUri(stringDatatypeUri));        
        editConfiguration.addField(new FieldVTwo().
                setName("coauthorUri"));
    }


    private List<List<String>> getPublicationTypeLiteralOptions() {
        List<List<String>> literalOptions = new ArrayList<List<String>>();
        literalOptions.add(list("http://vivoweb.org/ontology/core#Abstract", "Abstract"));
        literalOptions.add(list("http://purl.org/ontology/bibo/AcademicArticle", "Academic Article"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Article", "Article"));
        literalOptions.add(list("http://purl.org/ontology/bibo/AudioDocument", "Audio Document"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#BlogPosting", "Blog Posting"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Book", "Book"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#CaseStudy", "Case Study"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Catalog", "Catalog"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Chapter", "Chapter"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#ConferencePaper", "Conference Paper"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#ConferencePoster", "Conference Poster"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Database", "Database"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Dataset", "Dataset"));
        literalOptions.add(list("http://purl.org/ontology/bibo/EditedBook", "Edited Book"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#EditorialArticle", "Editorial Article"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Film", "Film"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Newsletter", "Newsletter"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#NewsRelease", "News Release"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Patent", "Patent"));
        literalOptions.add(list("http://purl.obolibrary.org/obo/OBI_0000272", "Protocol"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Report", "Report"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#ResearchProposal", "Research Proposal"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Review", "Review"));
        literalOptions.add(list("http://purl.obolibrary.org/obo/ERO_0000071 ", "Software"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Speech", "Speech"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Thesis", "Thesis"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#Video", "Video"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Webpage", "Webpage"));
        literalOptions.add(list("http://purl.org/ontology/bibo/Website", "Website"));
        literalOptions.add(list("http://vivoweb.org/ontology/core#WorkingPaper", "Working Paper"));
        return literalOptions;
    }

    //Form specific data
    public void addFormSpecificData(EditConfigurationVTwo editConfiguration, VitroRequest vreq) {

		HashMap<String, Object> formSpecificData = new HashMap<String, Object>();
        formSpecificData.put("editMode", getEditMode(vreq).name().toLowerCase());
        formSpecificData.put("sparqlForAcFilter", getSparqlForAcFilter(vreq));
        // stolen from AddEditorsToInformationResourceGenerator und AddAuthorsToInformationResourceGenerator respectively
        formSpecificData.put("existingEditorInfo", getExistingEditorships(editConfiguration.getSubjectUri(), vreq));
        formSpecificData.put("existingAuthorInfo", getExistingAuthorships(editConfiguration.getSubjectUri(), vreq));
		editConfiguration.setFormSpecificData(formSpecificData);


	}

    public String getSparqlForAcFilter(VitroRequest vreq) {
        String subject = EditConfigurationUtils.getSubjectUri(vreq);

        String query = "PREFIX core:<" + vivoCore + "> " +
        "SELECT ?pubUri WHERE { " +
        "<" + subject + "> core:relatedBy ?authorshipUri . " +
        "?authorshipUri a core:Authorship . " +
        "?authorshipUri core:relates ?pubUri . }";
        return query;
    }

    public EditMode getEditMode(VitroRequest vreq) {
        return EditModeUtils.getEditMode(vreq, list("http://vivoweb.org/ontology/core#relates"));
    }

    final static String publicationTitleQuery =
        "SELECT ?title WHERE { \n" +
        "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/bibo/Document> .\n" + 
        "?subject <" + label + "> ?title .  } ";

    final static String publicationTypeQuery =
        "SELECT ?pubType WHERE { \n" +
        "?subject <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> ?pubType . }";

    final static String publicationPublishedInURIQuery =
        "SELECT ?pubVenueUri WHERE { \n" +
        "?subject <http://vivoweb.org/ontology/core#hasPublicationVenue> ?pubVenueUri .  } ";

    final static String publicationPublishedInLabelQuery =
        "SELECT ?pubVenueLabel WHERE { \n" +
        "?subject <http://vivoweb.org/ontology/core#hasPublicationVenue> ?pubVenueUri . \n" +
        "?pubVenueUri <" + label + "> ?pubVenueLabel .  } ";

    final static String publicationPresentedAtURIQuery =
        "SELECT ?pubPresentedAtUri WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/presentedAt> ?pubPresentedAtUri .  } ";


    final static String publicationPresentedAtLabelQuery =
        "SELECT ?pubPresentedAtLabel WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/presentedAt> ?pubPresentedAtUri . \n" +
        "?pubPresentedAtUri <" + label + "> ?pubPresentedAtLabel .  } ";


    final static String publicationPublisherURIQuery =
        "SELECT ?pubPublisherUri WHERE { \n" +
        "?subject <http://vivoweb.org/ontology/core#publisher> ?pubPublisherUri .  } ";

    final static String publicationPublisherLabelQuery =
        "SELECT ?pubPublisherLabel WHERE { \n" +
        "?subject <http://vivoweb.org/ontology/core#publisher> ?pubPublisherUri .  \n" +
        "?pubPublisherUri <" + label + "> ?pubPublisherLabel .  } ";


    final static String publicationLocationQuery =
        "SELECT ?location WHERE { \n" +
        "?subject <http://vivoweb.org/ontology/core#placeOfPublication> ?location .  } ";

    final static String publicationNumberQuery =
        "SELECT ?issue WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/issue> ?issue .  } ";

    final static String publicationIssueQuery =
        "SELECT ?no WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/edition> ?no .  } ";

    final static String publicationVolumeQuery =
        "SELECT ?volume WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/volume> ?volume .  } ";

    final static String publicationISBNQuery =
        "SELECT ?isbn WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/isbn13> ?isbn .  } ";

    final static String publicationISSNQuery =
        "SELECT ?issn WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/issn> ?issn .  } ";

    final static String publicationStartPageQuery =
        "SELECT ?startPage WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/pageStart> ?startPage .  } ";

    final static String publicationEndPageQuery =
        "SELECT ?endPage WHERE { \n" +
        "?subject <http://purl.org/ontology/bibo/pageEnd> ?endPage .  } ";

    final static String publicationYearQuery =
        "SELECT ?y WHERE { \n" +
        "?subject <http://vivoweb.org/ontology/core#dateTimeValue> ?dtv . \n" +
        "?dtv <http://vivoweb.org/ontology/core#dateTime> ?y .  } ";


    // taken freely from the other generators
    private static String EDITORSHIPS_MODEL = ""
			+ "PREFIX core: <http://vivoweb.org/ontology/core#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "CONSTRUCT\n"
			+ "{\n"
			+ "    ?subject core:relatedBy ?editorshipURI .\n"
            + "    ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "    ?editorshipURI a core:Editorship .\n"
			+ "    ?editorshipURI core:relates ?editorURI .\n"
			+ "    ?editorshipURI core:rank ?rank.\n"
			+ "    ?editorURI a foaf:Agent .\n"
			+ "    ?editorURI rdfs:label ?editorName .\n"
			+ "}\n"
			+ "WHERE\n"
			+ "{\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?editorshipURI .\n"
            + "        ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?editorshipURI a core:Editorship .\n"
			+ "        ?editorshipURI core:relates ?editorURI .\n"
			+ "        ?editorURI a foaf:Agent .\n"
			+ "    }\n"
			+ "    UNION\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?editorshipURI .\n"
            + "        ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?editorshipURI a core:Editorship .\n"
			+ "        ?editorshipURI core:relates ?editorURI .\n"
			+ "        ?editorURI a foaf:Agent .\n"
			+ "        ?editorURI rdfs:label ?editorName .\n"
			+ "    }\n"
			+ "    UNION\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?editorshipURI .\n"
            + "        ?subject a<http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?editorshipURI a core:Editorship .\n"
			+ "        ?editorshipURI core:rank ?rank.\n"
			+ "    }\n"
			+ "}\n";

    private static String EDITORSHIPS_QUERY = ""
        + "PREFIX core: <http://vivoweb.org/ontology/core#> \n"
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
        + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
        + "SELECT ?editorshipURI (REPLACE(STR(?editorshipURI),\"^.*(#)(.*)$\", \"$2\") AS ?editorshipName) ?editorURI ?editorName ?rank \n"
        + "WHERE { \n"
        + "?subject a <http://purl.org/ontology/bibo/Document> .\n"
        + "?subject core:relatedBy ?editorshipURI . \n"
        + "?editorshipURI a core:Editorship . \n"
        + "?editorshipURI core:relates ?editorURI . \n"
        + "?editorURI a foaf:Agent . \n"
        + "OPTIONAL { ?editorURI rdfs:label ?editorName } \n"
        + "OPTIONAL { ?editorshipURI core:rank ?rank } \n"
        + "} ORDER BY ?rank";


    private List<EditorshipInfo> getExistingEditorships(String subjectUri, VitroRequest vreq) {
		RDFService rdfService = vreq.getRDFService();

		List<Map<String, String>> editorships = new ArrayList<Map<String, String>>();
		try {
			String constructStr = QueryUtils.subUriForQueryVar(EDITORSHIPS_MODEL, "subject", subjectUri);

			Model constructedModel = ModelFactory.createDefaultModel();
			rdfService.sparqlConstructQuery(constructStr, constructedModel);

			String queryStr = QueryUtils.subUriForQueryVar(this.getEditorshipsQuery(), "subject", subjectUri);
			log.debug("Query string is: " + queryStr);

			QueryExecution qe = QueryExecutionFactory.create(queryStr, constructedModel);
			try {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					QuerySolution soln = results.nextSolution();
					RDFNode node = soln.get("editorshipURI");
					if (node.isURIResource()) {
						editorships.add(QueryUtils.querySolutionToStringValueMap(soln));
					}
				}
			} finally {
				qe.close();
			}
        } catch (Exception e) {
            log.error(e, e);
        }
        
		// editorships = QueryUtils.removeDuplicatesMapsFromList(editorships, "editorShipURI", "editorURI");
		log.debug("editorships = " + editorships);
        return getEditorshipInfo(editorships);
    }

	private List<EditorshipInfo> getEditorshipInfo(
			List<Map<String, String>> editorships) {
		List<EditorshipInfo> info = new ArrayList<EditorshipInfo>();
	 	String editorshipUri =  "";
	 	String editorshipName = "";
	 	String editorUri = "";
	 	String editorName = "";

		for ( Map<String, String> editorship : editorships ) {
		    for (Entry<String, String> entry : editorship.entrySet() ) {
		            if ( entry.getKey().equals("editorshipURI") ) {
		                editorshipUri = entry.getValue();
		            }
		            else if ( entry.getKey().equals("editorshipName") ) {
		                editorshipName = entry.getValue();
		            }
		            else if ( entry.getKey().equals("editorURI") ) {
		                editorUri = entry.getValue();
		            }
		            else if ( entry.getKey().equals("editorName") ) {
		                editorName = entry.getValue();
		            }
			 }

			 EditorshipInfo aaInfo = new EditorshipInfo(editorshipUri, editorshipName, editorUri, editorName);
		    info.add(aaInfo);
		 }
		 log.debug("info = " + info);
		 return info;
	}

	//This is the information about editors the form will require
	public class EditorshipInfo {
		//This is the editorship node information
		private String editorshipUri;
		private String editorshipName;
		//Editor information for editorship node
		private String editorUri;
		private String editorName;

		public EditorshipInfo(String inputEditorshipUri,
				String inputEditorshipName,
				String inputEditorUri,
				String inputEditorName) {
			editorshipUri = inputEditorshipUri;
			editorshipName = inputEditorshipName;
			editorUri = inputEditorUri;
			editorName = inputEditorName;

		}

		//Getters - specifically required for Freemarker template's access to POJO
		public String getEditorshipUri() {
			return editorshipUri;
		}

		public String getEditorshipName() {
			return editorshipName;
		}

		public String getEditorUri() {
			return editorUri;
		}

		public String getEditorName() {
			return editorName;
		}
	}

    protected String getEditorshipsQuery() {
    	return EDITORSHIPS_QUERY;
    }


private static String AUTHORSHIPS_MODEL = " \n"
			+ "PREFIX core: <http://vivoweb.org/ontology/core#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "PREFIX vcard:  <http://www.w3.org/2006/vcard/ns#>\n"
			+ "CONSTRUCT\n"
			+ "{\n"
			+ "    ?subject core:relatedBy ?authorshipURI .\n"
            + "    ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "    ?authorshipURI a core:Authorship .\n"
			+ "    ?authorshipURI core:relates ?authorURI .\n"
			+ "    ?authorshipURI core:rank ?rank .\n"
			+ "    ?authorURI a ?type .\n"
			+ "    ?authorURI rdfs:label ?authorName .\n"
			+ "    ?authorURI vcard:hasName ?vName .\n"
			+ "    ?vName vcard:givenName ?firstName .\n"
			+ "    ?vName vcard:familyName ?lastName .\n"
			+ "    ?vName core:middleName ?middleName .\n"
			+ "}\n"
			+ "WHERE\n"
			+ "{\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?authorshipURI .\n"
            + "        ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?authorshipURI a core:Authorship .\n"
			+ "        ?authorshipURI core:relates ?authorURI .\n"
			+ "        ?authorURI a foaf:Agent .\n"
			+ "        ?authorURI a ?type .\n"
			+ "    }\n"
			+ "    UNION\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?authorshipURI .\n"
            + "        ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?authorshipURI a core:Authorship .\n"
			+ "        ?authorshipURI core:relates ?authorURI .\n"
			+ "        ?authorURI a foaf:Agent .\n"
			+ "        ?authorURI rdfs:label ?authorName\n"
			+ "    }\n"
			+ "    UNION\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?authorshipURI .\n"
            + "        ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?authorshipURI a core:Authorship .\n"
			+ "        ?authorshipURI core:rank ?rank\n"
			+ "    }\n"
			+ "    UNION\n"
			+ "    {\n"
			+ "        ?subject core:relatedBy ?authorshipURI .\n"
            + "        ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "        ?authorshipURI a core:Authorship .\n"
			+ "        ?authorshipURI core:relates ?authorURI .\n"
			+ "        ?authorURI a vcard:Individual .\n"
			+ "        ?authorURI a ?type .\n"
			+ "        ?authorURI vcard:hasName ?vName .\n"
			+ "        ?vName vcard:givenName ?firstName .\n"
			+ "        ?vName vcard:familyName ?lastName .\n"
			+ "    }\n"
			+ "    UNION\n"
			+ "    {\n"
			+ "         ?subject core:relatedBy ?authorshipURI .\n"
            + "         ?subject a <http://purl.org/ontology/bibo/Document> .\n"
			+ "         ?authorshipURI a core:Authorship .\n"
			+ "         ?authorshipURI core:relates ?authorURI .\n"
			+ "         ?authorURI a vcard:Individual .\n"
			+ "         ?authorURI a ?type .\n"
			+ "         ?authorURI vcard:hasName ?vName .\n"
			+ "         ?vName core:middleName ?middleName .\n"
			+ "    }\n"
			+ "}\n"
	;

    private static String AUTHORSHIPS_QUERY = " \n"
        + "PREFIX core: <http://vivoweb.org/ontology/core#> \n"
        + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
        + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
        + "PREFIX vcard:  <http://www.w3.org/2006/vcard/ns#> \n"
        + "SELECT ?authorshipURI (REPLACE(STR(?authorshipURI),\"^.*(#)(.*)$\", \"$2\") AS ?authorshipName) ?authorURI ?authorName ?rank \n"
        + "WHERE { { \n"
       // + "  ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/ontology/bibo/Document> .\n"
        + "  ?subject core:relatedBy ?authorshipURI . \n"
        + "  ?authorshipURI a core:Authorship . \n"
        + "  ?authorshipURI core:relates ?authorURI . \n"
        + "  ?authorURI a foaf:Agent . \n"
        + "  OPTIONAL { ?authorURI rdfs:label ?authorName } \n"
        + "  OPTIONAL { ?authorshipURI core:rank ?rank } \n"
	    + "} UNION {  \n"
	    + "	 ?subject core:relatedBy ?authorshipURI .  \n"
	    + "	 ?authorshipURI a core:Authorship .  \n"
	    + "	 ?authorshipURI core:relates ?authorURI .  \n"
	    + "	 ?authorURI a vcard:Individual .  \n"
	    + "	 ?authorURI vcard:hasName ?vName . \n"
	    + "	 ?vName vcard:givenName ?firstName . \n"
	    + "	 ?vName vcard:familyName ?lastName . \n"
	    + "	 OPTIONAL { ?vName core:middleName ?middleName . } \n"
	    + "	 OPTIONAL { ?authorshipURI core:rank ?rank }  \n"
	    + "	 bind ( COALESCE(?firstName, \"\") As ?firstName1) . \n"
	    + "	 bind ( COALESCE(?middleName, \"\") As ?middleName1) . \n"
	    + "	 bind ( COALESCE(?lastName, \"\") As ?lastName1) . \n"
	    + "	 bind (concat(str(?lastName1 + \", \"),str(?middleName1 + \" \"),str(?firstName1)) as ?authorName) . \n"
        + "} } ORDER BY ?rank";


    private List<AuthorshipInfo> getExistingAuthorships(String subjectUri, VitroRequest vreq) {
		RDFService rdfService = vreq.getRDFService();

		List<Map<String, String>> authorships = new ArrayList<Map<String, String>>();
		try {
			String constructStr = QueryUtils.subUriForQueryVar(AUTHORSHIPS_MODEL, "subject", subjectUri);

			Model constructedModel = ModelFactory.createDefaultModel();
			rdfService.sparqlConstructQuery(constructStr, constructedModel);

			String queryStr = QueryUtils.subUriForQueryVar(this.getAuthorshipsQuery(), "subject", subjectUri);
			log.debug("Query string is: " + queryStr);

			QueryExecution qe = QueryExecutionFactory.create(queryStr, constructedModel);
			try {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					QuerySolution soln = results.nextSolution();
					RDFNode node = soln.get("authorshipURI");
					if (node.isURIResource()) {
						authorships.add(QueryUtils.querySolutionToStringValueMap(soln));
					}
				}
			} finally {
				qe.close();
			}
        } catch (Exception e) {
            log.error(e, e);
        }
        // authorships = QueryUtils.removeDuplicatesMapsFromList(authorships, "authorShipURI", "authorURI");
        log.debug("authorships = " + authorships);
        return getAuthorshipInfo(authorships);
    }

	private List<AuthorshipInfo> getAuthorshipInfo(
			List<Map<String, String>> authorships) {
		List<AuthorshipInfo> info = new ArrayList<AuthorshipInfo>();
	 	String authorshipUri =  "";
	 	String authorshipName = "";
	 	String authorUri = "";
	 	String authorName = "";

		for ( Map<String, String> authorship : authorships ) {
		    for (Entry<String, String> entry : authorship.entrySet() ) {
		            if ( entry.getKey().equals("authorshipURI") ) {
		                authorshipUri = entry.getValue();
		            }
		            else if ( entry.getKey().equals("authorshipName") ) {
		                authorshipName = entry.getValue();
		            }
		            else if ( entry.getKey().equals("authorURI") ) {
		                authorUri = entry.getValue();
		            }
		            else if ( entry.getKey().equals("authorName") ) {
		                authorName = entry.getValue();
		            }
			 }

			 AuthorshipInfo aaInfo = new AuthorshipInfo(authorshipUri, authorshipName, authorUri, authorName);
		    info.add(aaInfo);
		 }
		 log.debug("info = " + info);
		 return info;
	}

	//This is the information about authors the form will require
	public class AuthorshipInfo {
		//This is the authorship node information
		private String authorshipUri;
		private String authorshipName;
		//Author information for authorship node
		private String authorUri;
		private String authorName;

		public AuthorshipInfo(String inputAuthorshipUri,
				String inputAuthorshipName,
				String inputAuthorUri,
				String inputAuthorName) {
			authorshipUri = inputAuthorshipUri;
			authorshipName = inputAuthorshipName;
			authorUri = inputAuthorUri;
			authorName = inputAuthorName;

		}

		//Getters - specifically required for Freemarker template's access to POJO
		public String getAuthorshipUri() {
			return authorshipUri;
		}

		public String getAuthorshipName() {
			return authorshipName;
		}

		public String getAuthorUri() {
			return authorUri;
		}

		public String getAuthorName() {
			return authorName;
		}
	}

    protected String getAuthorshipsQuery() {
    	return AUTHORSHIPS_QUERY;
    }

}


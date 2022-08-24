### Archictecture

```mermaid

sequenceDiagram
    Web Client->>IDM: Get bibliographical data
    IDM->>VIVO: send request to VIVO's Data Distribution API
    VIVO->>IDM: deliver JSON data
    IDM->>Web Client: convert JSON to BibTex, XML or RIS and send back to client


```

## Data Distribution API

```sql
:data_distributor_publications_by_author
    a   <java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor> ,
        <java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromContentDistributor> ;
    :actionName "publications_by_author" ;
    :query """
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
PREFIX foaf:     <http://xmlns.com/foaf/0.1/>
PREFIX hsmw:     <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#>
PREFIX kdsf:     <http://kerndatensatz-forschung.de/owl/Basis#>
PREFIX vivo:     <http://vivoweb.org/ontology/core#>
PREFIX obo:      <http://purl.obolibrary.org/obo/>
PREFIX bibo:    <http://purl.org/ontology/bibo/>
PREFIX vivo-de: <http://vivoweb.org/ontology/core/de#>

                SELECT  DISTINCT
                        ?uri
                        ?type
						(SAMPLE(?pYear) AS ?publicationYear)
						(GROUP_CONCAT(DISTINCT ?aName; separator = "; ") AS ?authorName)
						(SAMPLE(?pubtitle) AS ?label)
						(SAMPLE(?pubIn) AS ?publishedIn)
						(SAMPLE(?biboVolume) AS ?volume)
						(SAMPLE(?biboIssue) AS ?issue)
						(SAMPLE(?biboEdition) AS ?edition)
						(SAMPLE(?pageStart) AS ?uritartPage)
						(SAMPLE(?pageEnd) AS ?endPage)
						(SAMPLE(?_publisher) AS ?publisher)
						(GROUP_CONCAT(DISTINCT ?_editor; separator = "; ") AS ?editor)
						(SAMPLE(?_doi) AS ?doi)
						(SAMPLE(?_issn) AS ?issn)
						(SAMPLE(?_isbn10) AS ?isbn10)
						(SAMPLE(?_isbn13) AS ?isbn13)
						WHERE
                        {
                                ?uri a bibo:Document .
								?uri <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType> ?type . 
                                ?uri rdfs:label ?pubtitle .
                                OPTIONAL {
                                        ?uri vivo:dateTimeValue ?dtv .
                                        ?dtv vivo:dateTime ?dt .
                                        BIND(xsd:dateTime(?dt) AS ?date)
                                        BIND(year(?date) AS ?pYear)
                                }
  							    OPTIONAL {
  									?uri bibo:pageStart ?pageStart . 
  								}
								OPTIONAL {
  									?uri bibo:pageEnd ?pageEnd . 
  								}
								OPTIONAL {
  									?uri bibo:volume ?biboVolume . 
  								}
  								OPTIONAL {
  									?uri bibo:issue ?biboIssue . 
  								}
								OPTIONAL {
  									?uri bibo:edition ?biboEdition . 
  								}
  								OPTIONAL {
    									?uri vivo:hasPublicationVenue ?pubvenue .
    									?pubvenue rdfs:label ?pubIn .
  										OPTIONAL {
      										?pubvenue vivo:publisher ?_pv_publishernode .
      										?_pv_publishernode a foaf:Organization .
      										?_pv_publishernode rdfs:label ?_pv_publisher .
    									}
										OPTIONAL {
      										?pubvenue vivo:relatedBy ?_pv_editorrole_node .
      										?_pv_editorrole_node a vivo:Editorship .
      										OPTIONAL {
        											?_pv_editorrole_node vivo:relates ?_pv_editornode .
      												?_pv_editornode a foaf:Person .
      												?_pv_editornode rdfs:label ?_pv_editor .
      										}
      									    OPTIONAL {
        											?_pv_editorrole_node vivo:relates ?_pv_editornode .
      												?_pv_editornode a foaf:Organization .
      												?_pv_editornode rdfs:label ?_pv_editor .
      										}
    									}
    									OPTIONAL {
      										?pubvenue bibo:isbn10 ?_pv_isbn10 .
    									}
    									OPTIONAL {
      										?pubvenue bibo:isbn13 ?_pv_isbn13 .
    									}
        								OPTIONAL {
      										?pubvenue bibo:issn ?_pv_issn .
    									}
  								}
    							OPTIONAL {
      										?uri vivo:publisher ?_doc_publishernode .
      										?_doc_publishernode a foaf:Organization .
      										?_doc_publishernode rdfs:label ?_doc_publisher .
    							}
  								OPTIONAL {
      										?uri vivo:relatedBy ?_doc_editorrole_node .
      										?_doc_editorrole_node a vivo:Editorship .
      								OPTIONAL {
    											?_doc_editorrole_node vivo:relates ?_doc_editornode .
    											?_doc_editornode a foaf:Person .
      											?_doc_editornode rdfs:label ?_doc_editor .
    								}
          							OPTIONAL {
    											?_doc_editorrole_node vivo:relates ?_doc_editornode .
    											?_doc_editornode a foaf:Organization .
      											?_doc_editornode rdfs:label ?_doc_editor .
    								}
  								}

  								OPTIONAL {
    									?uri bibo:doi ?_doi .
  								}
  							  OPTIONAL {
      										?uri bibo:isbn10 ?_doc_isbn10 .
    						  }
    						 OPTIONAL {
      										?uri bibo:isbn13 ?_doc_isbn13 .
    						 }
        					 OPTIONAL {
      										?uri bibo:issn ?_doc_issn .
    							}
                                ?uri vivo:relatedBy ?authorship .
  								?authorship a vivo:Authorship .
                                ?authorship vivo:relates  ?theAuthor .
  								BIND (COALESCE(?_pv_publisher, ?_doc_publisher) AS ?_publisher)
  								BIND (COALESCE(?_pv_editor, ?_doc_editor) AS ?_editor)
  								BIND (COALESCE(?_pv_issn, ?_doc_issn) AS ?_issn)
    							BIND (COALESCE(?_pv_isbn10, ?_doc_ibsn10) AS ?_isbn10)
    							BIND (COALESCE(?_pv_isbn13, ?_doc_isbn13) AS ?_isbn13)
  								OPTIONAL {
      									?uri vivo:relatedBy ?aship.
      									?aship a vivo:Authorship .
      									OPTIONAL {
    										?aship vivo:relates ?auth .
      										?auth a foaf:Person .
      										?auth rdfs:label ?aName .
    									 }
    									OPTIONAL {
      										?aship vivo:relates ?auth .
    										?auth a <http://www.w3.org/2006/vcard/ns#Individual> .
      										?auth <http://www.w3.org/2006/vcard/ns#hasName> ?vcardname .
      										?vcardname <http://www.w3.org/2006/vcard/ns#givenName> ?firstname .
          									?vcardname <http://www.w3.org/2006/vcard/ns#familyName> ?familyname .
      										BIND(CONCAT(?familyname, ", ", ?firstname) AS ?aName)
    									}
  								}
							} GROUP BY ?uri ?type
                        ORDER BY DESC(?publicationYear)
        """;
        :uriBinding "theAuthor" .
```

## Rails Methode (aufzurufen im Controller)

```
def my_bibtex
        @user = NdbUser.find_by_login(request.env['REMOTE_USER']) unless @user

        base_url = 'https://vivo-development.hs-mittweida.de/vivo/api/dataRequest/'

        # publication base
        action = 'publications_by_author'
        
        # the uri of the author
        authorUri = @user.vivo_uri.gsub(/[\<\>]/, '')
        puts authorUri

        # type mappings VIVO -> BibTex . TODO: complete the list
        type_mappings =  {
                "http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Sammelbandbeitrag" => "incollection",
                "http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#WissenschaftlicheVortragsfolien" => "inproceedings",
        }
        
        # mappings attribute VIVO -> BibTex
        attribute_mappings = {
            "publicationYear" => "year",
            "authorName" => "author",
            "label" => "title" ,
            "publishedIn" => "booktitle",
            "volume" => "volume",
            "issue" => "number",
            "edition" => "edition",
            "publisher" => "publisher",
            "editor" => "editor",
            "doi" => "doi",
            "issn" => "issn",
            "isbn10" => "isbn",
            "isbn13" => "isbn"
        }
        
        # get the id card picture of a user
        response = RestClient::Request.execute method: 'GET', url: base_url + action + "?theAuthor=" + authorUri
        response = JSON::parse(response)
        
        if response["results"] && response["results"]["bindings"] then
            
            result = ""
            response["results"]["bindings"].each do |publication|
        
                if type_mappings.has_key?(publication["type"]["value"]) then
                    
                    document_type = type_mappings[publication["type"]["value"]]
        
                    citation = publication["uri"]["value"].gsub(/[^A-Za-z0-9]/, '').upcase
                
                    result += "@" + document_type + "{" + citation + ",\n"
        
                    attribute_mappings.each do |k, v|
        
                        if publication.has_key?(k) then
                            result += "\t" + v + " = \"" + publication[k]["value"] + "\",\n" 
                        end
        
                    end
        
                    result += "}\n\n"
                end
            end
        
            render :text => result, :content_type => "text/html"
        end
    end

```

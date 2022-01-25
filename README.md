# VIVO-HSMW: Equipment

Einrichten über "Einzelseiten-Verwaltung" und Bearbeiten der Seite "Equipment":

 * "Benutzerdefinierte Vorlage erfordert Inhalt" und Hinterlegen des Templates "hsmw-equipment-v2.ftl"
 * Setzen des Typs auf SPARQL-Query-Ergebnisse
 * Query:
  
  ```sql
  PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd:      <http://www.w3.org/2001/XMLSchema#>
PREFIX owl:      <http://www.w3.org/2002/07/owl#>
PREFIX swrl:     <http://www.w3.org/2003/11/swrl#>
PREFIX swrlb:    <http://www.w3.org/2003/11/swrlb#>
PREFIX vitro:    <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
PREFIX p1:       <http://kerndatensatz-forschung.de/owl/Aggregationen#>
PREFIX p2:       <http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#>
PREFIX bibo:     <http://purl.org/ontology/bibo/>
PREFIX c4o:      <http://purl.org/spar/c4o/>
PREFIX cito:     <http://purl.org/spar/cito/>
PREFIX dcterms:  <http://purl.org/dc/terms/>
PREFIX event:    <http://purl.org/NET/c4dm/event.owl#>
PREFIX fabio:    <http://purl.org/spar/fabio/>
PREFIX foaf:     <http://xmlns.com/foaf/0.1/>
PREFIX geo:      <http://aims.fao.org/aos/geopolitical.owl#>
PREFIX hsmw:     <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#>
PREFIX kdsf-meta: <http://kerndatensatz-forschung.de/owl/Meta#>
PREFIX kdsf:     <http://kerndatensatz-forschung.de/owl/Basis#>
PREFIX kdsf-vivo: <http://lod.tib.eu/onto/kdsf/>
PREFIX p3:       <http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Meta#>
PREFIX obo:      <http://purl.obolibrary.org/obo/>
PREFIX ocrer:    <http://purl.org/net/OCRe/research.owl#>
PREFIX ocresst:  <http://purl.org/net/OCRe/statistics.owl#>
PREFIX ocresd:   <http://purl.org/net/OCRe/study_design.owl#>
PREFIX ocresp:   <http://purl.org/net/OCRe/study_protocol.owl#>
PREFIX ro:       <http://purl.obolibrary.org/obo/ro.owl#>
PREFIX skos:     <http://www.w3.org/2004/02/skos/core#>
PREFIX swo:      <http://www.ebi.ac.uk/efo/swo/>
PREFIX vcard:    <http://www.w3.org/2006/vcard/ns#>
PREFIX vitro-public: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>
PREFIX vivo-de:  <http://vivoweb.org/ontology/core/de#>
PREFIX vivo:     <http://vivoweb.org/ontology/core#>
PREFIX scires:   <http://vivoweb.org/ontology/scientific-research#>
PREFIX vann:     <http://purl.org/vocab/vann/>

#
# This example query gets 20 geographic locations
# and (if available) their labels
#
SELECT ?uri ?label ?date
WHERE
{
      ?uri a <http://vivoweb.org/ontology/core#Equipment> .
	  ?uri rdfs:label ?label .
	  FILTER NOT EXISTS {
    	?uri <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Ausserbetriebnahme> ?date .
  }
 } ORDER BY ?label
  ```

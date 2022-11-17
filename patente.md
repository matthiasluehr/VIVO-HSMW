# Patente 

## Dokumentation Harvester

* https://wiki.lyrasis.org/display/VIVO/VIVO+Harvester+User+Guide+1.0
* https://wiki.lyrasis.org/display/VIVO/UF+Grant+Data+to+VIVO+XSL+Information
* https://wiki.lyrasis.org/display/VIVO/IP+to+VIVO+XSL+Information


## Dokumentation SPARQL Update API

* https://wiki.lyrasis.org/vivodoc19x/reference/vivo-apis/sparql-update-api


Ist dem Harvester vermutlich vorzuziehen, da VIVO nicht offline gehen muss. Der Harvester schreibt direkt in den Triplestore. Das birgt zum einen ein gewisses Restrisiko und zum anderen braucht der Harvester dann exklusiven Zugriff auf den Store, was bedeutet, dass der Tomcat gestoptt werden muss.

Im Zusammenspiel mit der SPARQL Query API lassen sich dann gescriptet externe Datenquellen mit VIVO abgleichen. Die dafür notwendige Logik residiert dann im jeweiligen Script und man spart sich diverse für den Harvester notwendige Abstraktionslevels (CSV -> JDBC -> XSLT -> RDF etc).


SPARQL File:

```sql
update=DELETE DATA {
   GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> {
      <https://vivo.hs-mittweida.de/vivo/individual/patent-2>
            <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
                <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Patent> .
      <https://vivo.hs-mittweida.de/vivo/individual/patent-2>
            <http://www.w3.org/2000/01/rdf-schema#label>
                "My awful IP right" .
    }
}
INSERT DATA {
   GRAPH <http://vitro.mannlib.cornell.edu/default/vitro-kb-2> {
      <https://vivo.hs-mittweida.de/vivo/individual/patent-2>
            <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
                <https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Patent> .
      <https://vivo.hs-mittweida.de/vivo/individual/patent-2>
            <http://www.w3.org/2000/01/rdf-schema#label>
                "My awesome IP right" .
    }
}

```

Bemerkung: funktioniert so nicht wie angegeben in der Dokumentation. Man kann aber zwei Requests gegen die API absetzen. Einen, um mit DELETE obsolete Triples zu löschen und einen, um die aktualisierten bzw. völlig neue Triples mit INSERT einzufügen.

curl Aufruf:

```shell
curl -i -d 'email=vivo@hs-mittweida.de' -d 'password=XXXX' -d '@insert.sparql' 'https://vivo.hs-mittweida.de/vivo/api/sparqlUpdate'
```

## Dokumentation Patent-Ontology

Zuerst etwas legendäres:

```mermaid
    graph TD
    L1([data property])
    L2[class]
````

```mermaid
graph TD
    A[Patent]
    AB([rdfs:label])
    AC([hsmw:Anmeldetag])
    AD([hsmw:Offenlegungstag])
    AE([hsmw:Veroeffentlichungstag])
    AB-->A
    AC-->A
    AD-->A
    AE-->A
    F[hsmw:RolleDesErfinders]
    G[foaf:Person]
    H[foaf:Person]
    A--vivo:relates-->F
    A--vivo:relates-->I
    I[hsmw:RolleDesErfinders]
    F--vivo:relates-->G
    I--vivo:relates-->H
```

Faux Properties:

Aus  http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata :

```
<http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp1982>
        a                        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyDisplayConfig> ;
        vitro:displayLimitAnnot  0 ;
        vitro:displayRankAnnot   0 ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#displayName>
                "hat Erfinder" ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#listViewConfigFile>
                "listViewConfig-inventor.xml" ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#propertyGroup>
                <http://vivoweb.org/ontology#vitroPropertyGroupoverview> .

<http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp2015>
        a       <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ConfigContext> ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#configContextFor>
                vivo:relates ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasConfiguration>
                <http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp1982> ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#qualifiedBy>
                hsmw:RolleDesErfinders ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#qualifiedByDomain>
                hsmw:Schutzrecht .

<http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp1263>
        a                        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyDisplayConfig> ;
        vitro:displayLimitAnnot  0 ;
        vitro:displayRankAnnot   0 ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#displayName>
                "hat erfunden" ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#listViewConfigFile>
                "listViewConfig-invention.xml" ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#propertyGroup>
                <http://vivoweb.org/ontology#vitroPropertyGroupresearch> .

<http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp1273>
        a       <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ConfigContext> ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#configContextFor>
                vivo:relatedBy ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasConfiguration>
                <http://vitro.mannlib.cornell.edu/ns/vitro/siteConfig/fp1263> ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#qualifiedBy>
                hsmw:RolleDesErfinders ;
        <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#qualifiedByDomain>
                foaf:Person .
```

## Typo3 Anbindung mittels Data Distribution API

### Endpukte zum Abfragen von Daten aus VIVO

* Publikationen nach Forschungsschwerpunkt
 
 ```shell
 curl "https://vivo.hs-mittweida.de/vivo/api/dataRequest/publications_by_fsp?fspUri=https://vivo.hs-mittweida.de/vivo/individual/fsp01"
 ```

* Publikationsdetails

```shell
curl "https://vivo.hs-mittweida.de/vivo/api/dataRequest/publication_details?pubUri=https://vivo.hs-mittweida.de/vivo/individual/lhm-publication-32"
```

* Autorenliste von Publikationen

```shell
curl "https://vivo.hs-mittweida.de/vivo/api/dataRequest/publication_authors?pubUri=https://vivo.hs-mittweida.de/vivo/individual/lhm-publication-32"
```

* Personendetails

```shell
curl "https://vivo.hs-mittweida.de/vivo/api/dataRequest/person_details?personUri=https://vivo.hs-mittweida.de/vivo/individual/person_idm484"
```

* Projekte nach Forschungsschwerpunkt
* Projektdetails
* Projekte nach Personen

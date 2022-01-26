
<#import "lib-vivo-form.ftl" as lvf>

<#--Retrieve certain edit configuration information-->
<#assign htmlForElements = editConfiguration.pageData.htmlForElements />
<#assign editMode = editConfiguration.pageData.editMode />
<#assign flagClearLabelForExisting = "flagClearLabelForExisting" />
<#assign sparqlForAcFilter = "" />
<#assign sparqlQueryUrl = "${urls.base}/ajax/sparqlQuery" />
<#assign blankSentinel = ">SUBMITTED VALUE WAS BLANK<" />
<#assign requiredHint = "<span class='requiredHint'> *</span>" />
<#assign choseType = "${i18n().chose_type_for_advsior_role}" />
<#assign submitButtonText = "${i18n().create_entry}" />
<#assign modificationTime = .now>
<#assign modificationDate = modificationTime?date>

<#assign subject = lvf.getFormFieldValue(editSubmission, editConfiguration, "subject")/>
<#assign title = lvf.getFormFieldValue(editSubmission, editConfiguration, "title")/>
<#assign publicationType = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationType")/>
<#assign publicationVenueURI = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationVenueURI")/>
<#assign publicationVenueLabel = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationVenueLabel")/>
<#assign publicationPresentedAtURI = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationPresentedAtURI")/>
<#assign publicationPresentedAtLabel = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationPresentedAtLabel")/>
<#assign publicationPublisherURI = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationPublisherURI")/>
<#assign publicationPublisherLabel = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationPublisherLabel")/>
<#assign publicationLocation = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationLocation")/>
<#assign publicationIssue = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationIssue")/>
<#assign publicationNumber = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationNumber")/>
<#assign publicationVolume = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationVolume")/>
<#assign publicationISBN = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationISBN")/>
<#assign publicationISSN = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationISSN")/>
<#assign publicationStartPage = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationStartPage")/>
<#assign publicationEndPage = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationEndPage")/>
<#assign publicationYear = lvf.getFormFieldValue(editSubmission, editConfiguration, "publicationYear")/>

<#assign existingAuthorInfo = editConfiguration.pageData.existingAuthorInfo />
<#assign existingEditorInfo = editConfiguration.pageData.existingEditorInfo />


<#assign editorTypeSelect = [
			[ 'http://xmlns.com/foaf/0.1/Person',  i18n().person_capitalized ],
            [ 'http://vivoweb.org/ontology/core#Department',  i18n().department ],
            [ 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Arbeitskreis', i18n().arbeitskreis ],
            [ 'http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#AUF',  i18n().ausseruniversitaere_forschungseinrichtung ],
            [ 'http://vivoweb.org/ontology/core#GovernmentAgency',  i18n().behoerde ],
            [ 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Beirat', i18n().beirat ],
            [ 'http://vivoweb.org/ontology/core#AcademicDepartment',  i18n().fachbereich],
            [ 'http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#FH', i18n().fachhochschule],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().fakultaet ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().foerderorganisation ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().forschungs_organisation ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().gesellschaft ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().institut ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().klinische_einrichtung ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().kommitee ],
            [ 'http://vivoweb.org/ontology/core/de#Fakultaet', i18n().konsortium ],
            [ 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#LenkKreis', i18n().lenkungskreis ],
            [ 'http://vivoweb.org/ontology/core#Foundation', i18n().stiftung ],
            [ 'http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Universitaet', i18n().university],
            [ 'http://vivoweb.org/ontology/core#Company', i18n().unternehmen],
            [ 'http://vivoweb.org/ontology/core/de#ZentraleEinrichtung', i18n().zentrale_einrichtungen],
            [ 'http://vivoweb.org/ontology/core#Center', i18n().zentrum ]

		]
/>

<#assign publicationTypeSelect = [
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#ArbeitspapierForschungsbericht", i18n().kdsf_arbeitspapier],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#BeitragInWissenschaftlichenBlogs", i18n().beitrag_in_wiss_blogs],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#BeitraegeInterviewsInNicht-wissenschaftlichenMedien", i18n().kdsf_interviews],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Bibliographie", i18n().kdsf_bibliographie],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Editorial", i18n().kdsf_editorial],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Forschungsdaten", i18n().kdsf_datensammlung],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Journalartikel", i18n().kdsf_journalartikel],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Konferenzpaper", i18n().kdsf_konferenzpaper],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Konferenzposter", i18n().kdsf_konferenzposter],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#LetterToTheEditor", i18n().kdsf_letter_to_the_editor],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#MeetingAbstract", i18n().kdsf_meeting_abstract],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Monographie", i18n().kdsf_monographie],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Preprint", i18n().preprint],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Quellenedition", i18n().kdsf_quellenedition],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Review", i18n().kdsf_review],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Rezension", i18n().kdsf_rezension],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Sammelbandbeitrag", i18n().kdsf_sammelbandbeitrag],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Software", i18n().kdsf_software],
                        ["http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#WissenschaftlicheVortragsfolien", i18n().kdsf_wissenschaftliche_vortragsfolien]
]
/>

<#assign eventTypeSelect = [
                        ["http://purl.org/ontology/bibo/Conference", i18n().conference],
                        ["http://purl.org/ontology/bibo/Workshop", i18n().workshop],
                        ["https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Messe", "Messe"],
                        ["http://vivoweb.org/ontology/core#InvitedTalk", "Gastvortrag"],
                        ["http://vivoweb.org/ontology/core#Meeting", "Besprechung"],
                        ["http://vivoweb.org/ontology/core#Presentation", "Vortrag"],
                        ["http://purl.org/ontology/bibo/Performance", i18n().performance]
]
/>

<!--
                            <option value="http://xmlns.com/foaf/0.1/Person">${i18n().person_capitalized}</option>
                            <option value="http://vivoweb.org/ontology/core#Department">${i18n().department}</option>
                            <option value="https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Arbeitskreis">${i18n().arbeitskreis}</option>
                            <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#AUF">${i18n().ausseruniversitaere_forschungseinrichtung}</option>
                            <option value="http://vivoweb.org/ontology/core#GovernmentAgency">http://vivoweb.org/ontology/core#GovernmentAgency</option>
                            <option value="https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Beirat">${i18n().beirat}</option>
                            <option value="http://vivoweb.org/ontology/core#AcademicDepartment">${i18n().fachbereich}</option>
                            <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#FH">${i18n().fachhochschule}</option>
                            <option value="http://vivoweb.org/ontology/core/de#Fakultaet">${i18n().fakultaet}</option>
                            <option value="http://vivoweb.org/ontology/core#FundingOrganization">${i18n().foerderorganisation}</option>
                            <option value="http://vivoweb.org/ontology/core#ResearchOrganization">${i18n().forschungs_organisation}</option>
                            <option value="http://vivoweb.org/ontology/core#Association">${i18n().gesellschaft}</option>
                            <option value="http://vivoweb.org/ontology/core#Institute">${i18n().institut}</option>
                            <option value="http://vivoweb.org/ontology/core#ClinicalOrganization">${i18n().klinische_einrichtung}</option>
                            <option value="http://vivoweb.org/ontology/core#Committee">${i18n().kommitee}</option>
                            <option value="http://vivoweb.org/ontology/core#Consortium">${i18n().konsortium}</option>
                            <option value="https://vivo.hs-mittweida.de/vivo/ontology/hsmw#LenkKreis">${i18n().lenkungskreis}</option>
                            <option value="http://vivoweb.org/ontology/core#Foundation">${i18n().stiftung}</option>
                            <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Universitaet">${i18n().university}</option>
                            <option value="http://vivoweb.org/ontology/core#Company">${i18n().unternehmen}</option>
                            <option value="http://vivoweb.org/ontology/core/de#ZentraleEinrichtung">${i18n().zentrale_einrichtungen}</option>
                            <option value="http://vivoweb.org/ontology/core#Center">${i18n().zentrum}</option>
-->

<#if editMode == "edit">
        <#assign titleVerb="${i18n().edit_capitalized}">
        <#assign submitButtonText="${i18n().save_changes}">
        <#assign disabledVal="disabled">
<#else>
        <#assign titleVerb="${i18n().create_capitalized}">
        <#assign submitButtonText="${i18n().create_entry}">
        <#assign disabledVal=""/>
</#if>

<script src="${urls.theme}/js/jquery.validate.js"></script> 

<#assign editMode = "edit"/>

EditMode: ${editMode}

<h2>${titleVerb}&nbsp;${i18n().publication_entry_for} ${editConfiguration.subjectName}</h2>
<!--
<ul>
    <li>Object: ${subject}</li>
    <li>${title}</li>
    <li>${publicationType}</li>
    <li>${publicationVenueURI}</li>
    <li>${publicationVenueLabel}</li>
    <li>${publicationPresentedAtURI}</li>
    <li>${publicationPresentedAtLabel}</li>
    <li>${publicationPublisherURI}</li>
    <li>${publicationPublisherLabel}</li>
    <li>${publicationLocation}</li>
    <li>Issue: ${publicationIssue}</li>
    <li>Volume: ${publicationVolume}</li>
    <li>Number: ${publicationNumber}</li>
    <li>ISBN: ${publicationISBN}</li>
    <li>ISSN: ${publicationISSN}</li>
    <li>Start: ${publicationStartPage}</li>
    <li>End: ${publicationEndPage}</li>
    <li>Jahr: ${publicationYear}</li>
</ul>
-->

<div class="container-hsmw-white">
    <form id="addPublicationToPerson" action="${submitUrl}" class="customForm noIE67" role="add/edit Publication">

        <div class="row">
            <span class="col-sm-2"><label for="publicationType" class="hsmwLabel">${requiredHint} Art der Publikation</label></span>
            <span class="col-sm-4">
                    <select id="typeSelector" name="pubType" acGroupName="publication">
                        <option value="" <#if publicationType == "">selected</#if>>${i18n().type_selection}</option>
                            <#list publicationTypeSelect as type>
                    	        <option value="${type[0]}" <#if type[0] == publicationType> selected</#if>>${type[1]}</option>
                	        </#list>
                    </select>
            </span>
            <span class="col-sm-1" style="padding-left: 0px; margin-top: 15px; margin-left: -16px;">
                    <a href="https://vivo.hs-mittweida.de/tutorials/09-publikationstypen-in-vivohsmw/" onclick="window.open(this.href, 'Hilfe Publikationstypen', 'width=600,height=400,left=1100,top=300'); return false" title="Hilfe zu den Publikationstypen"><span class="hsmw-help">?</span></a>
            </span>
        </div>
        <div class="row">
            <span class="col-sm-2"><label for="title" class="hsmwLabel">${requiredHint} Titel</label></span>
            <span class="col-sm-10"><input type="text" size="100" id="title" name="title" <#if editMode == "edit">value="${title}"</#if>></span>
        </div>

        <div class="row">
            <span class="col-sm-2"><label for="book" class="hsmwLabel">Veröffentlicht in</label></span>
            <span class="col-sm-6"><input type="text" id="book" name="book" acGroupName="book" acHelptext="Vorhandene Quelle auswählen oder neue erstellen (Typ wählen)" class="acSelector"/><input class="display" type="hidden" id="bookDisplay" name="bookDisplay" acGroupName="book" value=""></span></span>
            <span class="col-sm-3">
                    <select style="width: 100%; margin-top: 0px;" name="bookType">
                        <option value=""selected="selected">${i18n().type_selection}</option>
                        <option value="http://purl.org/ontology/bibo/Journal">${i18n().bibo_zeitschrift}</option>
                        <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Konferenzband">${i18n().kdsf_konferenzband}</option>
                        <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#IntegrierendeRessource">${i18n().kdsf_loseblattsammlung}</option>
                        <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Monographie">${i18n().kdsf_monographie}</option>
                        <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#IntegrierendeRessource">${i18n().kdsf_online_resource}</option>
                        <option value="http://kerndatensatz-forschung.de/version1.2/technisches_datenmodell/owl/Basis#Sammelband">${i18n().kdsf_sammelband}</option>
                        <option value="https://vivo.hs-mittweida.de/vivo/ontology/hsmw#MassMedia">${i18n().bibo_zeitung}</option>
                    </select>
            </span>            
        </div>

        <div class="acSelection row" acGroupName="book">
                <span class="col-sm-2"><label for="acSelectionInfo" class="hsmwLabel">Veröffentlicht in</label></span>
                <span class="col-sm-10">
                        <code id="acSelectionInfo" class="acSelectionInfo hsmwAcSpan"></code>
                        <a href="" class="verifyMatch hsmwAcLink"  title="Übereinstimmung verifizieren">(${i18n().verify_match_capitalized}</a> oder
                        <a href="#" class="changeSelection hsmwAcLink" id="changeSelection">${i18n().change_selection})</a>
                </span>
                <input class="acUriReceiver" type="hidden" id="bookUri" name="bookUri" value="" ${flagClearLabelForExisting}="true" />
         </div> 

        <div class="row">
            <span class="col-sm-2"><label for="conference" class="hsmwLabel">Präsentiert während</label></span>
            <span class="col-sm-6"><input type="text" id="conferencelabel" name="conferencelabel" acGroupName="conference" class="acSelector" acGroupname="conference" acHelptext="${i18n().conference_hint}"><input class="display" type="hidden" id="conferenceDisplay" name="conferenceDisplay" acGroupName="conference" value=""></span>
            <span class="col-sm-3">
                    <select style="width: 100%; margin-top: 0px;" name="conferenceType">
                        <option value=""selected="selected">${i18n().type_selection}</option>
                        <option value="http://purl.org/ontology/bibo/Conference">${i18n().conference}</option>
                        <option value="http://purl.org/ontology/bibo/Workshop">${i18n().workshop}</option>
                        <option value="https://vivo.hs-mittweida.de/vivo/ontology/hsmw#Messe">Messe</option>
                        <option value="http://vivoweb.org/ontology/core#InvitedTalk">Gastvortrag</option>
                        <option value="http://vivoweb.org/ontology/core#Meeting">Besprechung</option>
                        <option value="http://vivoweb.org/ontology/core#Presentation">Vortrag</option>
                        <option value="http://purl.org/ontology/bibo/Performance">${i18n().performance}</option>
                    </select>
            </span>            
        </div>
        
        <div class="acSelection row" acGroupName="conference">
                <span class="col-sm-2"><label for="acSelectionInfo" class="hsmwLabel">Präsentiert während</label></span>
                <span class="col-sm-10">
                    <code id="acSelectionInfo" class="acSelectionInfo hsmwAcSpan"></code>
                    <a href="" class="verifyMatch hsmwAcLink"  title="Übereinstimmung verifizieren">(${i18n().verify_match_capitalized}</a> oder
                    <a href="#" class="changeSelection hsmwAcLink" id="changeSelection">${i18n().change_selection})</a> 
                </span>
                <input class="acUriReceiver" type="hidden" id="conferenceUri" name="conferenceUri" value="" ${flagClearLabelForExisting}="true" />
        </div>

<script type="text/javascript">
    var authorshipData = [];
</script>

<#if editMode == "edit">
    <#list existingAuthorInfo as authorship>
	    <#assign authorUri = authorship.authorUri/>
	    <#assign authorName = authorship.authorName/>

	    <div class="row authorship itemName">
		    	<#-- span.author will be used in the next phase, when we display a message that the author has been
			    removed. That text will replace the a.authorName, which will be removed. -->
			    <#if (authorUri?length > 0)>
				    <#assign aName = authorName/>
                    <span class="authorName col-sm-10">${authorName}</span>
			    <#else>
                    <#assign aName = authorship.authorshipName/>
				    <span class="authorName col-sm-10">${authorship.authorshipName}</span><em> (${i18n().no_linked_author})</em>
			    </#if>
                <span class="col-sm-2" authorshipUri="${authorship.authorshipUri}" authorName="${aName}" helpText="Möchten Sie diesen Autor wirklich entfernen:">
				    	<a href="${urls.base}/edit/primitiveDelete" class="remove" title="${i18n().remove_author_link}">${i18n().remove_capitalized}</a>
			    </span>
	    </div>

	    <script type="text/javascript">
		    	authorshipData.push({
			    		"authorshipUri": "${authorship.authorshipUri}",
				    	"authorUri": "${authorUri}",
				    	"authorName": "${authorName}"
			    });
	    </script>
    </#list>
</#if>    

            <div id="coauthor-div-0" name="coauthor-div-0" class="panel-collapse">
                
                <div class="row">
                    <span class="col-sm-2"><label class="coauthor hsmwLabel" for="coauthorlabel" class="hsmwLabel">${i18n().coauthor_capitalized}</span>
                    <span class="col-sm-6"><input type="text" class="acSelector" size="50"  id="coauthorlabel" name="coauthorlabel" acGroupName="co0" acHelptext="Vorhandene Person auswählen oder Nachname, Vorname eintragen"/><input class="display" type="hidden" id="co0Display" name="co0Display" acGroupName="co0" value=""></span>
                </div>
                <!-- div class="row">
                    <span class="col-sm-2"></span>
                    <span class="col-sm-2">
                        <a data-toggle="collapse" data-parent="#accordion" href="#coauthor-div-1"><span class="glyphicon glyphicon-plus"></span></a>
                        &nbsp;<span class="note">${i18n().add_coauthor_1}</span>
                    </span>
                </div -->
                
                <div class="acSelection row" acGroupName="co0">
                    <span class="col-sm-2"><label for="acSelectionInfo" class="hsmwLabel">${i18n().coauthor_capitalized}</label></span>
                    <span class="col-sm-6">
                                <code id="acSelectionInfo" class="acSelectionInfo hsmwAcSpan"></code>
                                <a href="" class="verifyMatch hsmwAcLink"  title="Übereinstimmung verifizieren">(${i18n().verify_match_capitalized}</a> oder
                                <a href="#" class="changeSelection hsmwAcLink" id="changeSelection">${i18n().change_selection})</a>
                                <input class="acUriReceiver" type="hidden" id="coauthorUri" name="coauthorUri" value="" ${flagClearLabelForExisting}="true" />
                    </span>
                    <!-- span class="col-sm-3">
                        <a data-toggle="collapse" data-parent="#accordion" href="#coauthor-div-1"><span class="glyphicon glyphicon-plus"></span></a>
                        &nbsp;<div class="note">${i18n().add_coauthor_1}</div>
                    </span -->
                </div>
            
            </div>

<#if editMode == "edit">  
    <#list existingEditorInfo as editorship>
	    <#assign editorUri = editorship.editorUri/>
	    <#assign editorName = editorship.editorName/>

	    <div class="row editorship itemName">
		    	<#-- span.author will be used in the next phase, when we display a message that the author has been
			    removed. That text will replace the a.authorName, which will be removed. -->
			    <#if (editorUri?length > 0)>
				    <#assign eName = editorName/>
                    <span class="authorName col-sm-10">${editorName}</span>
			    <#else>
                    <#assign eName = editorship.editorshipName/>
				    <span class="authorName col-sm-10">${editorship.editorshipName}</span><em> (${i18n().no_linked_author})</em>
			    </#if>
                <span class="col-sm-2" authorshipUri="${editorship.editorshipUri}" authorName="${eName}" helpText="Möchten Sie diesen Editor wirklich entfernen:">
				    	<a href="${urls.base}/edit/primitiveDelete" class="remove" title="${i18n().remove_author_link}">${i18n().remove_capitalized}</a>
			    </span>
	    </div>
    </#list>
</#if>
            <div id="editor-div-0" name="editor-div-0" class="panel-collapse">
                <div class="row">
                    <span class="col-sm-2"><label class="editor ${i18n().type_selection}" for="editor">${i18n().editor_capitalized}</span>
                    <span class="col-sm-6"><input type="text" class="acSelector" size="50"  id="editorlabel" name="editorlabel" acGroupName="editor" acHelptext="Vorhandene HerausgeberIn auswählen oder neue erstellen (Typ wählen)"/><input class="display" type="hidden" id="editor0Display" name="editor0Display" acGroupName="editor0" value=""></span>
                    <span class="col-sm-3">
                        <select style="width: 100%; margin-top: 0px;" name="editorType">
                            <option value=""selected="selected">${i18n().type_selection}</option>
                            <#list editorTypeSelect as type>
                    	        <option value="${type[0]}">${type[1]}</option>
                	        </#list>
                        </select>
                    </span>
                </div>
                <!--
                    <span class="row">
                        <span class="col-sm-2"></span>    
                        <span class="col-sm-3">
                            <a data-toggle="collapse" data-parent="#accordion" href="#editor-div-1"><span class="glyphicon glyphicon-plus"></span></a>
                            &nbsp;<span class="note">${i18n().add_editor_1}</span>
                        </span>
                    </span>
                </div -->
                <div class="acSelection row" acGroupName="editor">
                    <span class="col-sm-2"><label for="acSelectionInfo" class="hsmwLabel">${i18n().editor_capitalized}</label></span>
                    <span class="col-sm-7">
                                <code id="acSelectionInfo" class="acSelectionInfo"></code>
                                <a href="" class="verifyMatch hsmwAcLink"  title="Übereinstimmung verifizieren">(${i18n().verify_match_capitalized}</a> oder
                                <a href="#" class="changeSelection hsmwAcLink" id="changeSelection">${i18n().change_selection})</a>
                                <input class="acUriReceiver" type="hidden" id="editorUri" name="editorUri" value="" ${flagClearLabelForExisting}="true" />
                    </span>
                    <!-- span class="row">
                        <span class="col-sm-2"></span>
                        <span class="col-sm-3">
                            <a data-toggle="collapse" data-parent="#accordion" href="#editor-div-1"><span class="glyphicon glyphicon-plus"></span></a>
                            &nbsp;<div class="note">${i18n().add_editor_1}</div>
                        </span>
                    </span -->
                </div>
            </div>
        </div>



        <div class="row">
            <span class="col-sm-2"><label for="publisher" class="hsmwLabel">${i18n().publisher_capitalized}</label></span>
            <span class="col-sm-7"><input type="text" class="acSelector" size="50"  id="publisher" name="publisher" acGroupName="publisher" acHelptext="Vorhandenen Verlag auswählen oder neuen erstellen"/><input class="display" type="hidden" id="publisherDisplay" name="publisherDisplay" acGroupName="publisher" value=""></span>
        </div>
        <div class="acSelection row" acGroupName="publisher">
                <span class="col-sm-2"><label for="acSelectionInfo" class="hsmwLabel">Verlag</label></span>
                <span class="col-sm-10">
                    <code id="acSelectionInfo" class="acSelectionInfo hsmwAcSpan"></code>
                    <a href="" class="verifyMatch hsmwAcLink"  title="Übereinstimmung verifizieren">(${i18n().verify_match_capitalized}</a> oder
                    <a href="#" class="changeSelection hsmwAcLink" id="changeSelection">${i18n().change_selection})</a> 
                </span>
                <input class="acUriReceiver" type="hidden" id="publisherUri" name="publisherUri" value="" ${flagClearLabelForExisting}="true" />
        </div>

      
        <div class="row">
            <span class="col-sm-2"><label for="locale" class="hsmwLabel">${i18n().place_of_publication}</label></span>
            <span class="col-sm-7"><input type="text" id="locale" name="locale" value="${publicationLocation}"/></span>
        </div>

        <div class="row">
            <span class="col-sm-2"><label for="edition" class="hsmwLabel">Ausgabe</label></span><span class="col-sm-2"><input type="text" id="edition" name="edition" value="${publicationIssue}"></span>
            <span class="col-sm-2"><label for="issue" class="hsmwLabel">${i18n().hsmw_issue_capitalized}</label></span><span class="col-sm-2"><input type="text" id="issue" name="issue" value="${publicationNumber}"></span>
            <span class="col-sm-1"><label for="volume" class="hsmwLabel">${i18n().volume_capitalized}</label></span><span class="col-sm-2"><input type="text" id="volume" name="volume" value="${publicationVolume}"></span>
        </div>

         <div class="row">
            <span class="col-sm-2"><label for="isbn" class="hsmwLabel">ISBN</label></span><span class="col-sm-2"><input type="text" id="isbn" name="isbn" helpText="XXX-X-XX-XXXXXX-X" value="${publicationISBN}"></span>
            <span class="col-sm-2"><label for="issn" class="hsmwLabel">ISSN</label></span><span class="col-sm-2"><input type="text" id="issn" name="issn" helpText="XXXX-XXXX" value="${publicationISSN}"></span>
        </div>
         <div class="row">
            <span class="col-sm-2"><label for="startPage" class="hsmwLabel">${i18n().start_page}</label></span><span class="col-sm-2"><span style="font-size: 1.2em;">S.</span> <input style="width: 80%;" type="text" id="startPage" name="startPage" value="${publicationStartPage}"></span>
            <span class="col-sm-2"><label for="endPage" class="hsmwLabel">Endseite</label></span><span class="col-sm-2"><span style="font-size: 1.2em;">S.</span> <input style="width: 80%;" type="text" id="endPage" name="endPage" class="inputWithHelptext" value="${publicationEndPage}"></span>
        </div>

        <div class="row">
            <span class="col-sm-2"><label for="dateTime-year" class="hsmwLabel">${requiredHint} Jahr</label></span><span class="col-sm-2"><input type="text" name="dateTime-year" id="dateTime-year" value="" size="4" maxlength="4" required number min="1900" max="2100" data-msg="Bitte geben Sie ein gültiges Veröffentlichungsjahr an!" helpText="JJJJ"/></span>
        </div>

        <div class="row">
            <span class="4">
                <p class="submit">
                    <input type="hidden" name = "editKey" value="${editKey}"/>
                    <input type="submit" id="submit" value="${submitButtonText}"/><span class="or"> ${i18n().or} </span><a class="cancel" href="${cancelUrl}" title="${i18n().cancel_title}">${i18n().cancel_link}</a>
                </p>
            </span>
        </div>
        <p id="requiredLegend" class="requiredHint">* ${i18n().required_fields}</p>
    </form>
</div>

<script type="text/javascript">
    var customFormData  = {
        sparqlForAcFilter: '${sparqlForAcFilter}',
        sparqlQueryUrl: '${sparqlQueryUrl}',
        acUrl: '${urls.base}/autocomplete?tokenize=true',
        acTypes: {  co0: 'http://xmlns.com/foaf/0.1/Person',
                    conference: 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#acPresentedAt',
                    book: 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#acPublishedIn',
                    editor: 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#acEditors',
                    publisher: 'https://vivo.hs-mittweida.de/vivo/ontology/hsmw#acPublishers' },
        editMode: '${editMode}',
        defaultTypeName: 'Quelle', // used in repair mode to generate button text
        // multipleTypeNames: {publication: 'Publikation', book: 'Buch', conference: 'Veranstaltung'},
        baseHref: '${urls.base}/individual?uri=',
        blankSentinel: '${blankSentinel}',
        flagClearLabelForExisting: '${flagClearLabelForExisting}'
    };
    var i18nStrings = {
        selectAnExisting: '${i18n().select_an_advisee}',
        orCreateNewOne: ' ',
        selectedString: '${i18n().selected}'
    };
    
    function clearElement(element) {
        element.value = "";
    }

    function removeStyle(element, style) {
        element.classList.remove(style)
    }
    
    </script>


${stylesheets.add('<link rel="stylesheet" href="${urls.base}/js/jquery-ui/css/smoothness/jquery-ui-1.12.1.css" />',
					'<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/customForm.css" />',
					'<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/autocomplete.css" />',
					'<link rel="stylesheet" href="${urls.base}/templates/freemarker/edit/forms/css/manageDragDropList.css" />')}

${scripts.add('<script type="text/javascript" src="${urls.base}/js/jquery-ui/js/jquery-ui-1.12.1.min.js"></script>',
              '<script type="text/javascript" src="${urls.theme}/js/customFormUtils.js"></script>',
              '<script type="text/javascript" src="${urls.base}/js/browserUtils.js"></script>',
              '<script type="text/javascript" src="${urls.theme}/js/HsmwCustomFormWithAutocomplete.js"></script>')}

${scripts.add('<script type="text/javascript" src="${urls.theme}/js/addHSMWAuthorsToInformationResource.js"></script>')}

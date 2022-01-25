<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for browsing individuals in class groups for menupages -->

<script>

function showWithLetter(letter) {
    const collection = document.getElementsByClassName("individual");
    for (let i = 0; i < collection.length; i++) {
        if (collection[i].hasAttribute("hsmw-list-item")) {
            l = collection[i].getAttribute("hsmw-list-item");
            if (l == letter) {
                collection[i].style.display = "block"; 
            } else {
                collection[i].style.display = "none";
            }
        }
    }
}

function showAll() {
    const collection = document.getElementsByClassName("individual");
    for (let i = 0; i < collection.length; i++) {
        collection[i].style.display = "block";
    }  
    
}

</script>


<#import "lib-string.ftl" as str>
<section id="menupage-intro" role="region">
	<h2>Equipment</h2>
</section>

<noscript>
<p style="padding: 20px 20px 20px 20px;background-color:#f8ffb7">${i18n().browse_page_javascript_one} <a href="${urls.base}/browse" title="${i18n().index_page}">${i18n().index_page}</a> ${i18n().browse_page_javascript_two}</p>
</noscript>

<section id="noJavascriptContainer" class="hidden">
<section id="browse-by" role="region">
    <nav role="navigation">
        <ul id="browse-classes">
		<li id="equipment">
			<a class="selected" href="#equipment">Equipment <span class="count-classes">(${equipment?size})</span></a>
		</li>	
        </ul>
        <nav id="alpha-browse-container" role="navigation">
            <h3 class="selected-class">Equipment</h3>
            <#assign alphabet = ["A", "B", "C", "D", "E", "F", "G" "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"] />
            <ul id="alpha-browse-individuals">
                <li><a href="#" class="selected" data-alpha="all" onClick='showAll();' title="${i18n().select_all}">${i18n().all}</a></li>
                <#list alphabet as letter>
                    <li><a href="#" data-alpha="${letter?lower_case}" onClick='showWithLetter("${letter?upper_case}");' title="${i18n().browse_all_starts_with(letter)}">${letter}</a></li>
                </#list>
            </ul>
        </nav>
    </nav>

    <section id="individuals-in-class" role="region">
        <ul role="list" class="hsmw-item-list">
		<#list equipment as row>
			<li class="individual" role="listitem" hsmw-list-item="${row["label"][0]}">
				<h1>
					<a href="${row["uri"]}">${row["label"]}</a><br />
				</h1>
			</li>
		</#list>
        </ul>
    </section>
</section>
</section>
<script type="text/javascript">
    $('section#noJavascriptContainer').removeClass('hidden');
</script>

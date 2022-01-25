<#import "lib-datetime.ftl" as dt>

<#assign itemUri = statement.object?substring(statement.object?last_index_of("/")+1)>
	<a href="/vivo/display/${itemUri}">${statement.devicename}</a>

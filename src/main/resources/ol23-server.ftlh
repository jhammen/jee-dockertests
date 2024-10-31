<server description="test server">
	<featureManager>
<#list example.features as feature>
		<feature>${feature}</feature>
</#list>		
	</featureManager>
	<httpEndpoint id="defaultHttpEndpoint" host="*"
		httpPort="9080" httpsPort="9443" />
	<applicationManager autoExpand="true" />
</server>
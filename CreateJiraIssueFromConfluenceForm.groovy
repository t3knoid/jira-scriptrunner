/* The following code defines a Jira REST endpoint that will capture data from a
   form submitted using Forms for Confluence by Adaptivist. The data will be
   used to create a Jira ticket.
   
   Create a Forms for Confluence form configuration containing the following custom
   fields: 
   
   - customTextField1 - Text field
   - customTextField2 - Text field
   - customselectlistfield - A multi-select field
   - customradiobuttonfield - Radio button field with values of Yes and No
   
   Configure a conditional that will show customTextField2 only if customradiobuttonfield
   is set to Yes.
   
   As a Confluence admin, go to General Configuration > Forms for Confluence and create a new form configuration. Configure as follows:
   - Destination Type	ScriptRunner for JIRA
   - JIRA Application Link	https://yourjirainstance
   - ScriptRunner Endpoint	taskIssueTrackingForm
   - Additonal Information HTML	$response   
*/
      
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParametersImpl
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.customfields.manager.OptionsManager

import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonSlurper
import groovy.transform.BaseScript
import static com.atlassian.jira.issue.IssueFieldConstants.COMPONENTS

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

taskIssueTrackingForm(httpMethod: "POST", groups: ["jira-administrators"]) { MultivaluedMap queryParams, String body ->
    
    def params = new IssueInputParametersImpl()
    def issueService = ComponentAccessor.getIssueService()
    def constantsManager = ComponentAccessor.getConstantsManager()
    def forms = new JsonSlurper().parseText(body) as Map<String,String> // Parse JSON
    def projectManager = ComponentAccessor.getProjectManager()    
    def appProperties = ComponentAccessor.getApplicationProperties()
    def project = ComponentAccessor.projectManager.getProjectObjByKey("PRJ")  // Jira project code
    def user = ComponentAccessor.jiraAuthenticationContext.loggedInUser  // Will use currently logged in use to create the ticket
    def jiraBaseURL = appProperties.getString(com.atlassian.jira.config.properties.APKeys.JIRA_BASEURL)
    def customFieldManager = ComponentAccessor.getCustomFieldManager() // Needed to access custom fields
    def optionsManager = ComponentAccessor.getOptionsManager() // Needed when enumerating field options    

    /*
      Initialize issue and standard fields
    */
    MutableIssue issue = ComponentAccessor.issueFactory.issue
    issue.issueTypeId = constantsManager.allIssueTypeObjects.findByName("Task").id  // Task issue type
    issue.projectObject = project
    issue.summary = forms.summary[0]
    issue.description = forms.description[0]

    /*
      Set custom fields
    */
    // Custom text field
    def customTextField1 = customFieldManager.getCustomFieldObject("customfield_12603")
    issue.setCustomFieldValue(customTextField1, forms.customtextfield1[0])

    // Custom select List (multiple choices) field
    // We have to enumerate choices before assigning
    def customSelectListField = customFieldManager.getCustomFieldObject("customfield_13000")
    def customSelectListFieldConfig = customSelectListField.getRelevantConfig(issue)
    def customSelectListFieldOption = optionsManager.getOptions(customSelectListFieldConfig).getOptionForValue(forms.customselectlistfield[0], null) // Enumerate selected choice
    issue.setCustomFieldValue(customSelectListField, [customSelectListFieldOption]) // Need to enclose the option in a collection for select list field
    
    // Custom Radio Buttons field with Yes/No values
    // We have to enumerate choices before assigning
    def customRadioButtonField = customFieldManager.getCustomFieldObject("customfield_12635")
    def customRadioButtonFieldConfig = customRadioButtonField.getRelevantConfig(issue)
    def customRadioButtonFieldOption = optionsManager.getOptions(customRadioButtonFieldConfig).getOptionForValue(forms.customradiobuttonfield[0], null) // Enumerate selected choice
    issue.setCustomFieldValue(billableCF, billableOption)

    // Since customRadioButtonField is optional
    // Set customTextField2 if customtextfield2 is set to Yes
    if (forms.customradiobuttonfield[0]?.trim() == "Yes") {
        def customTextField2 = customFieldManager.getCustomFieldObject("customfield_11504") // Optional field to set
        issue.setCustomFieldValue(customTextField2, forms.customtextfield2[0])
    }
    
    /*
      Set Component
    */
    
    // def componentManager = ComponentAccessor.getProjectComponentManager()
    // def component = componentManager.findByComponentName(project.getId(), forms.component[0])
    // issue.setComponent([component])

    /*
      Create issue
    */
    ComponentAccessor.issueManager.createIssueObject(user, issue)

    log.info "Created issue : ${issue.key}"
    def issueKey = issue.key
    // Display link to new ticket
    return Response.ok("Successfully created <a href='" + jiraBaseURL + "/browse/" + issueKey + "'>" + issueKey+ "</a>. Ensure to assign the ticket otherwise it will not be addressed.").type(MediaType.TEXT_HTML).build()    
}

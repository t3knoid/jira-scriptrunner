# jira-scriptrunner
Jira Scriptrunner scripts

## [CreateJiraIssueFromConfluenceForm.groovy](https://github.com/t3knoid/jira-scriptrunner/blob/main/CreateJiraIssueFromConfluenceForm.groovy)
This script a Jira REST endpoint that will capture data from a form submitted using Forms for Confluence by Adaptivist. The data will be used to create a Jira ticket.

### Forms for Confluence Form
This example requires creation of a Forms for Confluence form containing the following custom fields: 
- customTextField1 - Text field
- customTextField2 - Text field
- customselectlistfield - A multi-select field
- customradiobuttonfield - Radio button field with values of Yes and No
   
Configure a conditional that will show customTextField2 only if customradiobuttonfield is set to Yes.

### Forms for Confluence Configuration   
Create a Forms for Confluence configuration by As a Confluence admin, go to General Configuration > Forms for Confluence and create a new form configuration. Configure as follows:
| Setting | Value |
|---------|-------|
| Destination Type | ScriptRunner for JIRA |
| JIRA Application Link |	https://yourjirainstance |
| ScriptRunner Endpoint |	taskIssueTrackingForm |
| Additonal Information HTML | $response |

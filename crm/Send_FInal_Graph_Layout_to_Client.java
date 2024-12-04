string button.send_final_graph_layout(String projectId)
{
OrgTemplateId = "hj2ar110e61e5d2654cda84c6bc038ac72e4e";
//-------------------------------------------------
// Get Organization Details	
orgDetails = zoho.crm.invokeConnector("crm.getorg",Map());
orgDetailsResponse = orgDetails.get("response");
companyName = orgDetailsResponse.get("org").getJSON("company_name");
//-------------------------------------------------
// Extract key details like Contact Name, Deal Name
projectRecord = zoho.crm.getRecordById("Deals",projectId);
showName = projectRecord.get("Show").getJSON("name");
//-------------------------------------------------
//  Get account id
accountId = projectRecord.get("Account_Name").getJSON("id");
accountRecord = zoho.crm.getRecordById("Accounts",accountId);
//-------------------------------------------------
// 	Get contact id
contactId = projectRecord.get("Contact_Name").getJSON("id");
contactRecord = zoho.crm.getRecordById("Contacts",contactId);
//-------------------------------------------------
// Extract key details like Contact Name, Deal Name
contactName = projectRecord.get("Contact_Name").getJSON("name");
projectName = projectRecord.getJSON("Deal_Name");
if(!projectRecord.get("Upload_Final_Graph_Layout").isNull())
{
	crmFileUploadedID = projectRecord.get("Upload_Final_Graph_Layout").getJSON("file_Id");
}
else
{
	crmFileUploadedID = null;
}
//-------------------------------------------------
// Email Recipient
projectOwner = projectRecord.get("Owner").getJSOn("email");
customerEmail = ifNull(contactRecord.getJSON("Email"),null);
//-------------------------------------------------
// 	Get File uploaded on the CRM record
crmAttachment = invokeurl
[
	url :"https://www.zohoapis.eu/crm/v7/files?id=" + crmFileUploadedID + " "
	type :GET
	connection:"crm"
];
// 	
downloadAttachment = crmAttachment;
//-------------------------------------------------
// 	Mapped data and senf Email attached common attachemnt
fields = Map();
fields.put("Project_Name",projectName);
fields.put("Contact_Name",contactName);
fields.put("Organization_Name",companyName);
// 
dataMap = Map();
dataMap.put("data",fields);
// 
params = Map();
signerList = {};
signerObj1 = Map();
signerObj1.put("recipient_1","dhemleromapas.devtac@gmail.com");
signerObj1.put("action_type","approve");
signerObj1.put("language","eu");
// 
signerObj2 = Map();
signerObj2.put("recipient_2","omapasdhemler82@gmail.com");
signerObj2.put("recipient_name",contactName);
signerObj2.put("action_type","sign");
signerObj2.put("language","eu");
signerList = {signerObj1,signerObj2};
singerlistStr = "[" + signerList.toString() + "]";
//-------------------------------------------------
// Merge and Sign with common attachments
paramList = list();
paramMap1 = {"paramName":"common_attachments","content":downloadAttachment};
paramMap2 = {"paramName":"merge_data","content":dataMap.toString(),"Content-Type":"application/json","stringPart":"true","encodingType":"UTF-8"};
paramMap3 = {"paramName":"filename","content":"Final Graph Layout Document " + showName + "","stringPart":"true"};
paramMap4 = {"paramName":"signer_data","content":singerlistStr,"Content-Type":"application/json","stringPart":"true"};
paramMap5 = {"paramName":"sign_in_order","content":"true","stringPart":"true"};
// 
paramList.add(paramMap1);
paramList.add(paramMap2);
paramList.add(paramMap3);
paramList.add(paramMap4);
paramList.add(paramMap5);
// 
if(crmFileUploadedID != null)
{
	//-------------------------------------------------
	// Merge and Sign Document
	mergeAndSign = invokeurl
	[
		url :"https://www.zohoapis.eu/writer/api/v1/documents/" + OrgTemplateId + "/merge/sign"
		type :POST
		files:paramList
		connection:"zoho_writter_connection"
	];
	info mergeAndSign;
	signRequestId = mergeAndSign.get("records").getJSON("sign_request_id");
	documentStatus = mergeAndSign.get("records").getJSON("status");
	//-------------------------------------------------
	// Validate the Zoho Sign Credits 
	if(!mergeAndSign.get("error").isNull())
	{
		errorCode = mergeAndSign.get("error").getJSON("errorcode");
		if(mergeAndSign != errorCode)
		{
			return "Monthly sign document limit reached for your organisation. Please, buy add-on credits and try again.";
		}
	}
	else
	{
		errorCode = null;
		if(errorCode == null)
		{
			insertData = Map();
			insertData.put("Name","Final Graph Layout" + " - " + contactName + " - " + projectName);
			insertData.put("Currency","EUR");
			insertData.put("Email",customerEmail);
			insertData.put("zohosign__Document_Status",documentStatus);
			insertData.put("zohosign__Module_Record_ID",projectId.toString());
			insertData.put("zohosign__ZohoSign_Document_ID",signRequestId);
			insertData.put("zohosign__Module_Name","Deals");
			insertData.put("zohosign__Contact",contactRecord);
			insertData.put("zohosign__Account",accountRecord);
			insertData.put("zohosign__Deal",projectRecord);
			insertData.put("For_Final_Graph_Layout",true);
			// 
			creatRecord = zoho.crm.createRecord("zohosign__ZohoSign_Documents",insertData,Map(),"crm");
			info creatRecord;
			return "You have successfully send the Final Graph Layout";
		}
	}
}
else
{
	return "Upload the Final Graph Layout first, then try again";
}
return "";
}
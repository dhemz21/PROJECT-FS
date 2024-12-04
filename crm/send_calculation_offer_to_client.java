string button.Send_Calculation_Offer_to_Client_Phase_1(Int projectId)
{
//-------------------------------------------------
// 	Organization Writer Template ID
OrgTemplateId = "1z2m4658e6b3a11ba43a18b76b074fc5561be";
//-------------------------------------------------
// Fetch the project record based on the provided project ID
projectRecord = zoho.crm.getRecordById("Deals",projectId);
showName = projectRecord.get("Show").getJSON("name");
//-------------------------------------------------
// Fetch the account id
accountId = projectRecord.get("Account_Name").getJSON("id");
accountRecord = zoho.crm.getRecordById("Accounts",accountId);
//-------------------------------------------------
// Fetch the contact id
contactId = projectRecord.get("Contact_Name").getJSON("id");
contactRecord = zoho.crm.getRecordById("Contacts",contactId);
//-------------------------------------------------
// Extract key details like Contact Name, Customer Number, Phone, Email, and Closing Date
contactName = projectRecord.get("Contact_Name").getJSON("name");
customerName = accountRecord.get("Account_Name");
customerNumber = ifNull(accountRecord.getJSON("Customer_No"),null);
customerPhone = ifNull(accountRecord.getJSON("Phone"),null);
projectClosingDate = ifNull(projectRecord.get("Closing_Date"),null);
projectName = projectRecord.getJSON("Deal_Name");
generatedCalculationFile = ifNull(projectRecord.getJSON("Generated_Calculation_File_ID"),null);
//-------------------------------------------------
// Email Recipient
projectOwner = projectRecord.get("Owner").getJSOn("email");
customerEmail = ifNull(contactRecord.getJSON("Email"),null);
//-------------------------------------------------
// Fetch the project subform
currency_symbol = "€";
listOfMaterial = list();
productLineItems = projectRecord.get("Product_Line_Items");
for each  lineItems in productLineItems
{
	material = lineItems.get("Material").getJSON("name");
	total = lineItems.get("Total1").text("#,##0.00").replaceAll(",",".");
	convertToDecimal = total.substring(total.length() - 2,total.length());
	noDecimal = total.substring(0,total.length() - 3);
	fomattedTotal = noDecimal + "," + convertToDecimal + " " + currency_symbol;
	listOfMaterial.add({"Product_Line_Items.Material":material,"Product_Line_Items.Total":fomattedTotal});
}
//-------------------------------------------------
// Fetch records from worksheet
paramMap = Map();
paramMap.put('method','worksheet.records.fetch');
paramMap.put('worksheet_name','Sheet1');
paramMap.put('header_row',7);
paramMap.put('criteria',"\"Total\"!=\"\"");
paramMap.put('render_option','formatted');
paramMap.put('records_start_index',1);
paramMap.put('is_case_sensitive',false);
resourceId = generatedCalculationFile;
if(generatedCalculationFile != null)
{
	response = invokeurl
	[
		url :"https://sheet.zoho.eu/api/v2/" + resourceId
		type :POST
		parameters:paramMap
		connection:"zohosheet_connection"
	];
}
//-------------------------------------------------
// Map the data to the template fields
fields = Map();
fields.put("Project_Contact_Name",contactName);
fields.put("Project_Name",projectName);
fields.put("Customer_Number",customerNumber);
fields.put("Customer_Name",customerName);
fields.put("Customer_Phone",customerPhone);
fields.put("Customer_Email",customerEmail);
fields.put("Project_Date",projectClosingDate);
fields.put("Product_Line_Items",listOfMaterial);
//-------------------------------------------------
// Create another mapping for merging		
data = Map();
data.put("data",fields);
//-------------------------------------------------
//dummy recipients
dummyRecipient1 = "jamesberuega24@gmail.com";
dummyRecipient2 = "jamesberuega6@gmail.com";
// 
// Creating list of the Approver ans Signer 	
signerList = List();
signerObj1 = Map();
signerObj1.put("recipient_1",dummyRecipient1);
signerObj1.put("action_type","approve");
signerObj1.put("language","eu");
signerList.add(signerObj1);
//-------------------------------------------------
signerObj2 = Map();
signerObj2.put("recipient_2",dummyRecipient2);
signerObj2.put("recipient_name",contactName);
signerObj2.put("action_type","sign");
signerObj2.put("language","eu");
signerList.add(signerObj2);
//-------------------------------------------------
// Adding parameters on merging the document
params = Map();
params.put("merge_data",data);
params.put("service_name","zohosign");
params.put("filename","Calculation Offer to Client " + showName + "");
params.put("sign_in_order","true");
params.put("signer_data",signerList);
params.put("merge_data",data);
//-------------------------------------------------
// Validating the Calculation File ID to generate first before proceeding
if(generatedCalculationFile == null)
{
	return "Generate Calculation Offer to client first, then try again.";
}
else
{
	//-------------------------------------------------
	// Merge and Sign the Document
	mergeAndSign = invokeurl
	[
		url :"https://www.zohoapis.eu/writer/api/v1/documents/" + OrgTemplateId + "/merge/sign"
		type :POST
		parameters:params
		connection:"zoho_writter_connection"
	];
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
			insertData.put("Name","Calculation Offer" + " - " + contactName + " - " + projectName);
			insertData.put("Currency","EUR");
			insertData.put("zohosign__Document_Status",documentStatus);
			insertData.put("zohosign__Module_Record_ID",projectId.toString());
			insertData.put("zohosign__ZohoSign_Document_ID",signRequestId);
			insertData.put("zohosign__Module_Name","Deals");
			insertData.put("zohosign__Contact",contactRecord);
			insertData.put("zohosign__Account",accountRecord);
			insertData.put("zohosign__Deal",projectRecord);
			insertData.put("For_Calculation_File",true);
			//-------------------------------------------------
			// Create record to ZohoSign Document Module
			creatRecord = zoho.crm.createRecord("zohosign__ZohoSign_Documents",insertData,Map(),"crm");
			return "The Calculation Offer has been successfully sent to the client.";
		}
	}
}
return "";
}
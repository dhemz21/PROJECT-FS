void automation.create_activation_record(Int contactId)
{
//----------------------------------------------------
// Fetch the contact record using the given Contact ID
//----------------------------------------------------
contactRecord = zoho.crm.getRecordById("Contacts",contactId);
//----------------------------------------------------
// Extract necessary fields from the contact record
//----------------------------------------------------
contactActivationType = contactRecord.get("Activation_Type");
contactActivationDate = contactRecord.get("Activation_Date");
contactAccountId = contactRecord.get("Account_Name").getJSON("id");
//----------------------------------------------------
// Check if both Activation Type and Activation Date are available
//----------------------------------------------------
if(contactActivationType != null && contactActivationDate != null)
{
	//----------------------------------------------------
	// Create a map to hold data for the Activation record
	//----------------------------------------------------
	dataMap = Map();
	dataMap.put("Name",contactActivationType + " - " + contactActivationDate.toDate());
	dataMap.put("Activation_Date",contactActivationDate);
	dataMap.put("Activation_Type",contactActivationType);
	dataMap.put("Customer_Name",contactAccountId);
	dataMap.put("Contact",contactId);
	dataMap.put("Contact_ID",contactId.toString());
	dataMap.put("Activation_Status","Not Yet Started");
	//----------------------------------------------------
	// Optional fields set to null initially
	//----------------------------------------------------
	dataMap.put("Office_in_Charge",null);
	dataMap.put("Show",null);
	dataMap.put("Project_Type",null);
	//----------------------------------------------------
	// Search for existing Activation records linked to the same Contact ID
	//----------------------------------------------------
	getActivationRecord = zoho.crm.searchRecords("Activations","(Contact_ID:equals:" + contactId + ")");
	//----------------------------------------------------
	// Check if any activation records exist for the given contact
	//----------------------------------------------------
	if(getActivationRecord == null || getActivationRecord.isEmpty())
	{
		//----------------------------------------------------
		// No activation records found; create a new activation record
		//----------------------------------------------------
		info "No records found. Creating a new record.";
		createRecordToActivation = zoho.crm.createRecord("Activations",dataMap,Map(),"crm");
		info createRecordToActivation;
	}
	else
	{
		//----------------------------------------------------
		// Existing activation record(s) found; update the first record
		//----------------------------------------------------
		info "Record(s) found. Updating the first record.";
		activationId = getActivationRecord.get(0).get("id");
		//----------------------------------------------------
		// Update the existing record with the new data
		//----------------------------------------------------
		updateResponse = zoho.crm.updateRecord("Activations",activationId,dataMap,Map(),"crm");
		info updateResponse;
	}
}
}
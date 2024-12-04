void automation.update_activation_record(Int contactId)
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
// 
// 
//----------------------------------------------------
// Create a map to hold data for the Activation record
//----------------------------------------------------
dataMap = Map();
dataMap.put("Name",contactActivationType + " - " + contactActivationDate.toDate());
dataMap.put("Activation_Date",contactActivationDate);
dataMap.put("Activation_Type",contactActivationType);
dataMap.put("Contact",contactId);
dataMap.put("Customer_Name",contactAccountId);
dataMap.put("Contact_ID",contactId.toString());
//----------------------------------------------------
// Optional fields set to null initially
//----------------------------------------------------
dataMap.put("Office_in_Charge",null);
dataMap.put("Show",null);
dataMap.put("Project_Type",null);
// 	
//----------------------------------------------------
// Search for existing Activation records linked to the same Contact ID
//----------------------------------------------------
getActivationRecord = zoho.crm.searchRecords("Activations","(Contact_ID:equals:" + contactId + ")");
activationId = getActivationRecord.getJSON("id");
//----------------------------------------------------
// Update the existing record with the new data
//----------------------------------------------------
updateResponse = zoho.crm.updateRecord("Activations",activationId,dataMap,Map(),"crm");
info updateResponse;
}
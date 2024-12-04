string button.Generate_Calculation_File_Devtac(Int projectId)
{
//----------------------------------------------
// 	Organization Sheet Template ID
OrgVariable = zoho.crm.getOrgVariable("calculation_sheet_folder_ID");
//----------------------------------------------
// Fetch the project record based on the provided project ID
projectRecord = zoho.crm.getRecordById("Deals",projectId);
//----------------------------------------------
// Fetch the Product Line Items and Deal Name
projectName = projectRecord.get("Deal_Name");
productLineItems = projectRecord.get("Product_Line_Items");
if ( productLineItems.size() == 0 ) 
{
	return "Add Product Line Items first, then try again";
} else 
{
//----------------------------------------------
// Create a workbook from the created template
paramMap = Map();
paramMap.put('method','workbook.createfromtemplate');
paramMap.put('resource_id','hhd6fc8fc876e0a554d22ad469281cd2a0655');
paramMap.put('workbook_name',projectName);
createWorkBook = invokeurl
[
	url :"https://sheet.zoho.eu/api/v2/createfromtemplate"
	type :POST
	parameters:paramMap
	connection:"zohosheet_connection"
];
resourceID = createWorkBook.get("resource_id");
workbookStatus = createWorkBook.get("status");
//----------------------------------------------
// Validate the first before updating the workbook
if(createWorkBook != workbookStatus)
{
	//----------------------------------------------
	// Add content to particular cell in a specified worksheet.
	dataListDetails = list();
	paramMapCalculationDetails = Map();
	paramMapCalculationDetails.put('method','cells.content.set');
	//----------------------------------------------
	// Add data on specific cell
	dataShowName = Map();
	dataShowName.put('worksheet_id',"0#");
	dataShowName.put('content',"Rental equipment for: " + projectRecord.get("Show").getJSON("name"));
	dataShowName.put('row',"3");
	dataShowName.put('column',"2");
	dataListDetails.add(dataShowName);
	//----------------------------------------------
	// Add data on specific cell
	dataDetails = Map();
	dataDetails.put('worksheet_id',"0#");
	dataDetails.put('content',"Hall Number: " + projectRecord.getJSON("Hall") + "                    Stand Number: " + projectRecord.getJSON("Stand1") + "                                 Design: " + projectRecord.getJSON("Dimension") + "                                           Date: " + projectRecord.getJSON("Created_Time").toDate());
	dataDetails.put('row',"4");
	dataDetails.put('column',"2");
	dataListDetails.add(dataDetails);
	//----------------------------------------------
	// Invoking to add a content
	paramMapCalculationDetails.put('data',dataListDetails);
	resourceIdtesting = resourceID;
	responsetest = invokeurl
	[
		url :"https://sheet.zoho.eu/api/v2/" + resourceIdtesting
		type :POST
		parameters:paramMapCalculationDetails
		connection:"zohosheet_connection"
	];
	//----------------------------------------------
	// Creating a list of product lineitems and set to distinct
	categoryLists = list();
	for each  items in productLineItems
	{
		category = items.getJSON("Product_Category");
		categoryLists.add(category);
	}
	distinctCategoryLists = categoryLists.distinct();
	//----------------------------------------------
	// Loop the distinct product category
	number = 8;
	positionCounter = 1;
	dataLists = list();
	formatLists = list();
	lineItemsFormat = list();
	for each  distinctCategoryList in distinctCategoryLists
	{	
		//----------------------------------------------
		// Add content to particular cell in a specified worksheet.
		paramMap = Map();
		paramMap.put('method','cells.content.set');
		// 	
		datadistinctCategoryList = Map();
		datadistinctCategoryList.put('worksheet_id',"0#");
		datadistinctCategoryList.put('content',distinctCategoryList);
		datadistinctCategoryList.put('row',number);
		datadistinctCategoryList.put('column',"5");
		dataLists.add(datadistinctCategoryList);
		formatLists.add(number);
		// 	
		for each  productLineItem in productLineItems
		{
			if(productLineItem.getJSON("Product_Category") == distinctCategoryList)
			{
				//----------------------------------------------
				// Add content to particular cell in a specified worksheet.
				paramMap = Map();
				rowNumber = number + 1;
				lineItemsFormat.add(rowNumber);
				paramMap.put('method','cells.content.set');
				//----------------------------------------------
				// Add data specfic cells  			
				dataPosition = Map();
				dataPosition.put('worksheet_id',"0#");
				dataPosition.put('content',positionCounter);
				dataPosition.put('row',rowNumber);
				dataPosition.put('column',"2");
				dataLists.add(dataPosition);
				//----------------------------------------------
				// Add data specfic cells  				
				dataQuantity = Map();
				dataQuantity.put('worksheet_id',"0#");
				dataQuantity.put('content',productLineItem.getJSON("Qty"));
				dataQuantity.put('row',rowNumber);
				dataQuantity.put('column',"3");
				dataLists.add(dataQuantity);
				//----------------------------------------------
				// Add data specfic cells  			
				dataUnit = Map();
				dataUnit.put('worksheet_id',"0#");
				dataUnit.put('content',productLineItem.getJSON("Unit"));
				dataUnit.put('row',rowNumber);
				dataUnit.put('column',"4");
				dataLists.add(dataUnit);
				//----------------------------------------------
				// Add data specfic cells  			
				dataMaterial = Map();
				dataMaterial.put('worksheet_id',"0#");
				dataMaterial.put('content',productLineItem.getJSON("Material").getJSON("name"));
				dataMaterial.put('row',rowNumber);
				dataMaterial.put('column',"5");
				dataLists.add(dataMaterial);
				//----------------------------------------------
				// Add data specfic cells  				
				dataPrice = Map();
				dataPrice.put('worksheet_id',"0#");
				dataPrice.put('content',productLineItem.getJSON("Price").toLong());
				dataPrice.put('row',rowNumber);
				dataPrice.put('column',"6");
				dataLists.add(dataPrice);
				//----------------------------------------------
				// Add data specfic cells  			
				dataTotal = Map();
				dataTotal.put('worksheet_id',"0#");
				dataTotal.put('content',"=C" + rowNumber + "*F" + rowNumber);
				dataTotal.put('row',rowNumber);
				dataTotal.put('column',"7");
				dataLists.add(dataTotal);
				//----------------------------------------------
				// Add data specfic cells  		
				dataMaterialId = Map();
				dataMaterialId.put('worksheet_id',"0#");
				dataMaterialId.put('content',productLineItem.getJSON("Material").getJSON("id"));
				dataMaterialId.put('row',rowNumber);
				dataMaterialId.put('column',"8");
				dataLists.add(dataMaterialId);
				// 				
				number = number + 1;
				positionCounter = positionCounter + 1;		
			}
		}
		number = number + 1;
	}
	grandTotalRow = number + 1;
	dataGrantTotal = Map();
	dataGrantTotal.put('worksheet_id',"0#");
	dataGrantTotal.put('content',"=SUM(G8:G" + number + ")");
	dataGrantTotal.put('row',grandTotalRow);
	dataGrantTotal.put('column',"7");
	dataLists.add(dataGrantTotal);
	formatLists.add(number);
	//----------------------------------------------
	// Add content to multiple discontinuous cells.
	dataDescription = Map();
	dataDescription.put('worksheet_id',"0#");
	dataDescription.put('content',projectRecord.getJSON("Description") + projectRecord.get("Show").getJSON("name"));
	dataDescription.put('row',grandTotalRow);
	dataDescription.put('column',"5");
	dataLists.add(dataDescription);
	// 	
	paramMap.put('data',dataLists);
	resourceId = resourceID;
	response = invokeurl
	[
		url :"https://sheet.zoho.eu/api/v2/" + resourceId
		type :POST
		parameters:paramMap
		connection:"zohosheet_connection"
	];
	//----------------------------------------------
	// Adding formatting to the cells
	paramFormat = Map();
	paramFormat.put('method','ranges.format.set');
	dataListFormat = List();
	for each  formatList in formatLists
	{
		dataFormat = Map();
		dataFormat.put('worksheet_id',"0#");
		dataFormat.put('range',"B" + formatList + ":G" + formatList);
		dataFormat.put('fill_color',"#f79432");
		dataFormat.put('font_color',"#ffffff");
		dataListFormat.add(dataFormat);
	}
	//----------------------------------------------
	// Adding formatting to the cells
	for each  itemRow in lineItemsFormat
	{
		dataFormatRow = Map();
		dataFormatRow.put('worksheet_id',"0#");
		dataFormatRow.put('range',"B" + itemRow + ":G" + itemRow);
		dataFormatRow.put('font_color',"#105766");
		dataListFormat.add(dataFormatRow);
	}
	//----------------------------------------------
	// Add content to multiple discontinuous cells.
	dataFormatGrandTotal = Map();
	dataFormatGrandTotal.put('worksheet_id',"0#");
	dataFormatGrandTotal.put('range',"G" + grandTotalRow);
	dataFormatGrandTotal.put('bold',true);
	dataFormatGrandTotal.put('font_color',"#000000");
	dataListFormat.add(dataFormatGrandTotal);
	// 	
	dataFormatGrandTotal1 = Map();
	dataFormatGrandTotal1.put('worksheet_id',"0#");
	dataFormatGrandTotal1.put('range',"E" + grandTotalRow);
	dataFormatGrandTotal1.put('bold',true);
	dataFormatGrandTotal1.put('font_color',"#105766");
	dataListFormat.add(dataFormatGrandTotal1);
	// 
	paramFormat.put('format_json',dataListFormat);
	responseFormat = invokeurl
	[
		url :"https://sheet.zoho.eu/api/v2/" + resourceId
		type :POST
		parameters:paramFormat
		connection:"zohosheet_connection"
	];
	//----------------------------------------------
	// Add content to particular cell in a specified worksheet.
	dataListDetails = list();
	paramMapOtherDetails = Map();
	paramMapOtherDetails.put('method','cells.content.set');
	//----------------------------------------------
	// Add data specfic cells  		
	dataShowName = Map();
	dataShowName.put('worksheet_id',"0#");
	dataShowName.put('content',"fairservices.net GmbH");
	dataShowName.put('row',grandTotalRow + 1);
	dataShowName.put('column',"6");
	dataListDetails.add(dataShowName);
	//----------------------------------------------
	// Add data specfic cells  
	dataLocation = Map();
	dataLocation.put('worksheet_id',"0#");
	dataLocation.put('content',"Christian-Kremp-Str. 6a | D-35578 Wetzlar | Germany ");
	dataLocation.put('row',grandTotalRow + 2);
	dataLocation.put('column',"6");
	dataListDetails.add(dataLocation);
	//----------------------------------------------
	// Add data specfic cells  
	dataPhone = Map();
	dataPhone.put('worksheet_id',"0#");
	dataPhone.put('content',"fon: +49 6441 9827410 | info@fairservices.net");
	dataPhone.put('row',grandTotalRow + 3);
	dataPhone.put('column',"6");
	dataListDetails.add(dataPhone);
	//----------------------------------------------
	// Add data specfic cells  
	paramMapOtherDetails.put('data',dataListDetails);
	resourceIdtesting = resourceID;
	responsetest = invokeurl
	[
		url :"https://sheet.zoho.eu/api/v2/" + resourceIdtesting
		type :POST
		parameters:paramMapOtherDetails
		connection:"zohosheet_connection"
	];
	// -----------------------------------------------------
	// Update the Generated Calculation File field on CRM
	insertSheetLink = "https://sheet.zoho.eu/sheet/open/" + resourceID;
	insertDataMap = Map();
	insertDataMap.put("Generated_Calculation_File",insertSheetLink);
	insertDataMap.put("Generated_Calculation_File_ID",resourceID);
	inserLinkRecord = zoho.crm.updateRecord("Deals",projectId,insertDataMap,Map(),"crm");
	// 
	return "The Calculation file has been successfully created.";
}
else
{
	return "Creation of Calculation file failed, please try again.";
}

}
}
void schedule.sla_breach_email_notification()
{
// 	-------get records from project module
queryMap = Map();
queryMap.put("select_query","select Deal_Name, Owner, Stage from Deals where SLA_breach = true");
response = invokeurl
[
	url :"https://www.zohoapis.eu/crm/v7/coql"
	type :POST
	parameters:queryMap.toString()
	connection:"crm"
];
// 	-------- email template content and style
content_header = "<p>Hi Carsten,</p><br></br><p>We would like to inform you of an SLA breach concerning the following container project. Please review the details below:</p><br></br>";
content_footer = "<br></br<p>Please take the necessary action to address the breach and ensure compliance with the agreed SLA.</p><p>If you need any further details or assistance, feel free to reach out.</p><br></br><p>This email was generated automatically. Please do not reply to this message.</p>";
// 	--------- table column headers
templateTable = "<table style='border-collapse: collapse; width: 100%;'>";
templateTable = templateTable + "<tr style='background-color: #f4f4f4; color: #333;'>";
templateTable = templateTable + "<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>No.</th>";
templateTable = templateTable + "<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Project Name</th>";
templateTable = templateTable + "<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Record Owner</th>";
templateTable = templateTable + "<th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Current Stage</th>";
templateTable = templateTable + "</tr>";
row_counter = 1;
// 	--------- iterate retreived data and display it as rows in table
for each  data in response.get("data")
{
	user = zoho.crm.getRecordById("users",data.getJSON("Owner").getJSON("id"));
	userFullName = user.get("users").get(0).getJSON("full_name");
	templateTable = templateTable + "<tr>";
	templateTable = templateTable + "<td style='border: 1px solid #ddd; padding: 8px;'>" + row_counter + "</td>";
	templateTable = templateTable + "<td style='border: 1px solid #ddd; padding: 8px;'> <a href='https://crm.zoho.eu/crm/org20064687644/tab/Potentials/" + data.getJSON("id") + "' target='_blank'>" + data.getJSON("Deal_Name") + "</a></td>";
	templateTable = templateTable + "<td style='border: 1px solid #ddd; padding: 8px;'>" + userFullName + "</td>";
	templateTable = templateTable + "<td style='border: 1px solid #ddd; padding: 8px;'>" + data.getJSON("Stage") + "</td>";
	templateTable = templateTable + "</tr>";
	row_counter = row_counter + 1;
}
templateTable = templateTable + "</table>";
// 	---------- send mail function
sendmail
[
	from :zoho.adminuserid
	to :"arianegaye.villalino@devtac.com","james.beruega@devtac.com"
	subject :"SLA Breach Notification - Urgent Attention Required - " + zoho.currentdate
	message :content_header + templateTable + content_footer
]
}
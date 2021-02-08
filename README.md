
# AWRS-NOTIFICATION

[![Build Status](https://travis-ci.org/hmrc/awrs-notification.svg?branch=master)](https://travis-ci.org/hmrc/awrs-notification) [ ![Download](https://api.bintray.com/packages/hmrc/releases/awrs-notification/images/download.svg) ](https://bintray.com/hmrc/releases/awrs-notification/_latestVersion)

This is a placeholder README.md for a new repository

### License


This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

**AWRS Notification**
----
AWRS Notification API enables ETMP to push AWRS application status updates and/or changes to the customer resulting in the sending of an email to the applicant.

This API exposes one endpoint to send a templated email.


**Resource**

  POST   /:registrationNumber

**Example request URI**

  /awrs-notification/XFAW00000123456

**Accepted Media Types**

  application/json

**Path parameters**

| Parameter                                   | Type 	     | Description  |
| -------------------------------------- | ----------------  | ------------ |
| registrationNumber | String               | Unique AWRS identification number for a regitered user. Validation for registrationNumber: ^X[A-Z]AW00000[0-9]{6}$ |

**Request Body parameters**

| Parameter                                  | Type 	     | Description  |
| -------------------------------------- | ----------------  | ------------ |
| name 	(Required) | String               | Name of the applicant |
| email (Required) | String               | Email Address of the applicant to which Email needs to be sent |
| status (Optional) | String               | AWRS application Status. Valid Statuses: 04 (Approved), 05 (Approved with Conditions), 06 (Rejected), 07 (Rejected under Review/Appeal), 08 (Revoked), 09 (Revoked under Review/Appeal), 10 (De-registered)|
| contact_type (Optional) | String               | Type of notification. Valid contact types: "REJR", "REVR", "CONA", "MTRJ", "NMRJ", "MTRV", "NMRV", "OTHR" |
| contact_number (Optional) | String               | Contact number of the applicant|
| variation (Required) | Boolean               | Variation will be either true or false|


**Sample Request**


   ```javascript
	{
	  "name": "first name",
	  "email": "example@example.com",
	  "status": "04",
	  "contact_type": "REJR",
	  "contact_number": "123456789012",
	  "variation": false
	}
  ```

**Responses and Error Codes**
	
A successful response returns HTTP 200 without body.

| Code| Text 	       | Description  | 
| --- | ---------------| ------------ |
| 200 | OK             | Success      | 
| 204 | No Content     | Success      | 
| 400 | Bad Request             | The request was invalid and/or has not passed validation      |
| 404 | Not Found             | The resource requested does not exist      |
| 500 | Internal Server Error             | There is a problem with the service or dependent systems      |
| 503 | Service Unavailable             | The service or dependant systems are unavailable      | 


**Error Scenarios**

| Error Scenario| HTTP Status| Validation  | Error Message  |
| --- | ---------------| ------------ | ------------ |
|Invalid registration number format | 400 (Bad Request) | ^X[A-Z]{2}00000[0-9]{6}$ | Invalid registration number |
|Invalid name | 400 (Bad Request) | ^[A-Za-z0-9 ]{1,140}$ | Invalid name |
|Invalid email address| 400 (Bad Request) | Generic email validation, maximum length is 100 | Invalid email address |
|Invalid status | 400 (Bad Request) | 0[4-9] OR 10 | Invalid status |
|Invalid contact number | 400 (Bad Request) | [0-9]{12} | Invalid contact number |
|Invalid contact type | 400 (Bad Request) | Accepted contact types: "REJR", "REVR", "CONA", "MTRJ", "NMRJ", "MTRV", "NMRV", "OTHR" | Invalid contact type  |
|There is a problem with the service or dependent systems | 500 (Internal Server Error) | N/A | e.g. Unexpected Runtime Errors  |
|Template mapping not found for the contact type | 503 (Service Unavailable) | Currently just REJR template is supported | Template does not exist for provided contact type |
|The service or dependant systems are unavailable | 503 (Service Unavailable) | N/A | POST of 'http://example/send-templated-email' failed. Caused by: 'Connection refused' |


**Sample Response**

  **Code:** 200

  **Content:**
  No Content


**Example Error Response**

  **Code:** 400

  **Content:**

  ```javascript
	{
	 "reason": "Invalid registration number"
	}
  ```
  
**AWRS Notification Cache**
----

If the email template and AWRS reference number are valid and the Contact Type is one of either MTRJ, NMRJ, MTRV or NMRV, the notification will be stored to MONGO.

The notification is stored as the following fields, 

* registrationNumber
* contactNumber
* contactType
* status

e.g.

 ```json
	{ 
	 "registrationNumber" : "XXAW000001234560",
	 "contactNumber" : "123456789012",
	 "contactType" : "MTRJ",
	 "status" : "04"
	}
  ```
  
To retrieve a notification from MONGO you can call the endpoint as follows,
  
**Resource**
  
  GET   /cache/:registrationNumber
  
**Example request URI**
  
  /awrs-notification/cache/XFAW00000123456
  
**Accepted Media Types**
  
  application/json
  
**Path parameters**
  
| Parameter                                   | Type 	     | Description  |
| -------------------------------------- | ----------------  | ------------ |
| registrationNumber | String               | Unique AWRS identification number for a registered user. Validation for registrationNumber: ^X[A-Z]AW00000[0-9]{6}$ |

**Responses and Error Codes**
	
A successful response returns HTTP 200 with a JSON body containing the following fields, 

* registrationNumber
* contactNumber
* contactType
* status

e.g.

 ```json
	{ 
	 "registrationNumber" : "XXAW000001234560",
	 "contactNumber" : "123456789012",
	 "contactType" : "MTRJ",
	 "status" : "04"
	}
  ```

| Code| Text 	       | Description  | 
| --- | ---------------| ------------ |
| 204 | No Content     | Success      | 
| 404 | Not Found      | The notification was not found      |

To delete a notification from MONGO you can call the endpoint as follows,
  
**Resource**
  
  DELETE   /cache/:registrationNumber
  
**Example request URI**
  
  /awrs-notification/cache/XFAW00000123456
  
**Accepted Media Types**
  
  Not required - DO NOT specify application/json as it will give a Bad Request when calling the DELETE
  
**Path parameters**
  
| Parameter                                   | Type 	     | Description  |
| -------------------------------------- | ----------------  | ------------ |
| registrationNumber | String               | Unique AWRS identification number for a registered user. Validation for registrationNumber: ^X[A-Z]AW00000[0-9]{6}$ |

**Responses and Error Codes**
	
A successful response returns HTTP 200 without body.

| Code| Text 	                   | Description  | 
| --- | ---------------------------| ------------ |
| 200 | OK                         | Success (Even if the registration number does not exist in MONGO!) | 
| 500 | Internal Server Error      | An unexpected error occurred - good luck! |

**AWRS Notification Viewed Status**
----

When a notification is POSTed and if the email template and AWRS reference number are valid a corresponding 
notification viewed status will be stored in MONGO. It will be set to 'false' to indicate that it is new and not yet viewed.
The frontend application is responsible for calling the PUT endpoint once the notification has been viewed, 
this will update the flag in MONGO and cause the GET endpoint to return 'true'.  

The notification viewed status is stored in MONGO with the following fields, 

* registrationNumber
* viewed

e.g.

 ```json
	{ 
	 "registrationNumber" : "XXAW000001234560",
	 "viewed" : false
	}
  ```
  
To retrieve a notification viewed status from MONGO you can call the endpoint as follows,
  
**Resource**
  
  GET   /cache/viewed/:registrationNumber
  
**Example request URI**
  
  /awrs-notification/cache/viewed/XFAW00000123456
  
**Accepted Media Types**
  
  application/json
  
**Path parameters**
  
| Parameter                                   | Type 	     | Description  |
| -------------------------------------- | ----------------  | ------------ |
| registrationNumber | String               | Unique AWRS identification number for a registered user. Validation for registrationNumber: ^X[A-Z]AW00000[0-9]{6}$ |

**Responses and Error Codes**
	
A successful response returns HTTP 200 with a JSON body containing the following field, 

* viewed

e.g.

 ```json
	{ 
	 "viewed" : false
	}
  ```

| Code| Text 	       | Description  | 
| --- | ---------------| ------------ |
| 200 | OK             | Success      | 

To update a notification viewed status in MONGO and set it to 'true' you can call the endpoint as follows (note, no body is required as the status is always set to 'true'),
  
**Resource**
  
  PUT   /cache/viewed/:registrationNumber
  
**Example request URI**
  
  /awrs-notification/cache/viewed/XFAW00000123456
  
**Accepted Media Types**
  
  Not required - DO NOT specify application/json as it will give a Bad Request when calling the PUT
  
**Path parameters**
  
| Parameter                                   | Type 	     | Description  |
| -------------------------------------- | ----------------  | ------------ |
| registrationNumber | String               | Unique AWRS identification number for a registered user. Validation for registrationNumber: ^X[A-Z]AW00000[0-9]{6}$ |

**Responses and Error Codes**
	
A successful response returns HTTP 200 without body.

| Code| Text 	                   | Description  | 
| --- | ---------------------------| ------------ |
| 200 | OK                         | Success (Even if the registration number does not exist in MONGO!) | 
| 500 | Internal Server Error      | An unexpected error occurred - good luck! |


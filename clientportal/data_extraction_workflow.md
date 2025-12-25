## Workflow steps:
1. Once payment is successful from client and order status is submitted, trigger Data extraction workflow.
2.	Send documents to Nanonets for data extraction. Use webhooks to get response.
3.	Once the response is received, store extracted data in database in json format (extraction data for each document)
4.	Send order ready for review notification to accountant
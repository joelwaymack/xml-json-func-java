# XML to JSON Event Hub Processing

This repo contains a Java Function App that retrieves XML from an Event Hub, converts it to JSON, and then sends it to another EventHub.

To run this Function App locally, add a **local.settings.json** file in the root of the repository with the following format:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "storage_connection_string",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "XmlEventHub": "xml_event_hub_name",
    "XmlEventHubConnectionString": "xml_event_hub_connection_string",
    "JsonEventHub": "json_event_hub_name",
    "JsonEventHubConnectionString": "json_event_hub_connection_string"
  }
}
```

An HTTP request to **POST /api/orders** will generate a batch of 10 fake orders, serialize them as XML, and send them to the XML event hub to start the processing pipeline.
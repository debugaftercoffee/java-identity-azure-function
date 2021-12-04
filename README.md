# Getting Started
This Azure Function will return the list of containers from the storage account defined in the configuration `STORAGE_ACCOUNT_NAME`. The function's managed identity will need `Storage Blob Data Reader` RBAC on the storage account in order to list the containers. If you have configured a User-Assigned Managed Identity then set the `USER_ASSIGNED_MANAGED_ID` configuration to the *client id* of the identity. If `USER_ASSIGNED_MANAGED_ID` is not defined  the default credentials which are either credentials from *az login* if you are running local, or the System Managed Identity are used.

# Build and Run locally
This function requires Java 8 and the Azure Function 4.0 runtime.
```
mvn clean package
mvn azure-functions:run 
```
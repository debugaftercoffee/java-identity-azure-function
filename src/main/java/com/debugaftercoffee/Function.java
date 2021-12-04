package com.debugaftercoffee;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/containers". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/containers
     * 2. curl "{your host}/api/containers"
     */
    @FunctionName("containers")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        final String storageAccName = System.getenv("STORAGE_ACCOUNT_NAME");
        final String connectStr = String.format("https://%s.blob.core.windows.net", storageAccName);
        List<String> containerNames = new ArrayList<String>();

        try {
            TokenCredential credential = null;
            final String userAssignedManagedIdentity = System.getenv("USER_ASSIGNED_MANAGED_ID");
            if (userAssignedManagedIdentity == null) {
                context.getLogger().info("Using default credentials");
                final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
                credential = new DefaultAzureCredentialBuilder()
                    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                    .build();
            } else {
                context.getLogger().info(String.format("Using User Assigned Managed Identity - %s", userAssignedManagedIdentity));
                credential = new ManagedIdentityCredentialBuilder()
                    .clientId(userAssignedManagedIdentity)
                    .build();
            }

            context.getLogger().info(String.format("Building Blob Client - %s", connectStr));
            BlobServiceClient miBlobStorageClient = new BlobServiceClientBuilder()
                .endpoint(connectStr)
                .credential(credential)
                .buildClient();
            context.getLogger().info("Start listing containers");
            
            miBlobStorageClient.listBlobContainers().forEach(container -> containerNames.add(container.getName()));

            containerNames.forEach(container -> context.getLogger().info(String.format("Container Name: %s%n", container)));
            context.getLogger().info("Finished listing containers");
        } catch (Exception ex) {
            context.getLogger().info(String.format("Blob Error: %s", ex));
            QueryResponse resp = new QueryResponse(String.format("Blob Error: %s", ex), null);
            return request.createResponseBuilder(HttpStatus.SERVICE_UNAVAILABLE).body(resp).header("Content-Type", "application/json").build();
        }

        QueryResponse resp = new QueryResponse(String.format("Successfully queried storage account: %s", storageAccName), containerNames);
        return request.createResponseBuilder(HttpStatus.OK).body(resp).header("Content-Type", "application/json").build();
    }
}

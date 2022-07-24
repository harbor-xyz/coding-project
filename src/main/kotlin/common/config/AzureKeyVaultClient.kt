package common.config

import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.azure.security.keyvault.secrets.models.KeyVaultSecret

/**
 * Configuration client for getting/setting config values in Azure key vault.
 * Uses client-secret authentication.
 */
    class AzureKeyVaultClient(
    clientId: String,
    clientSecret: String,
    tenantId: String,
    vaultUrl: String
) {
    private val secretClient: SecretClient = SecretClientBuilder()
        .vaultUrl(vaultUrl)
        .credential(
            ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build()
        )
        .buildClient()

    /**
     * Get value for key.
     *
     * @param key
     * @return
     */
    fun getValue(key: String): String? {
        val keyVaultSecret: KeyVaultSecret? = secretClient.getSecret(key)
        return keyVaultSecret?.value
    }
}
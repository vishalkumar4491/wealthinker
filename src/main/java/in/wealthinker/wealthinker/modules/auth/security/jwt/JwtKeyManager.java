package in.wealthinker.wealthinker.modules.auth.security.jwt;

import in.wealthinker.wealthinker.config.JwtConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * JWT Key Manager for Asymmetric Signing
 *
 * PURPOSE:
 * - Manage RSA/ECDSA keys for JWT signing
 * - Load keys from files or keystore
 * - Support key rotation strategies
 * - Provide secure key storage
 *
 * SECURITY CONSIDERATIONS:
 * - Private keys must be kept secure
 * - Public keys can be distributed freely
 * - Keys should be rotated periodically
 * - Support for multiple key versions
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtKeyManager {

    private final JwtConfig jwtConfig;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyStore keyStore;

    @PostConstruct
    public void init() {
        if (jwtConfig.isAsymmetricSigning()) {
            try {
                loadKeys();
                log.info("JWT keys loaded successfully for asymmetric signing");
            } catch (Exception e) {
                log.error("Failed to load JWT keys", e);
                throw new IllegalStateException("JWT key initialization failed", e);
            }
        }
    }

    /**
     * Get private key for token signing
     */
    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new IllegalStateException("Private key not loaded");
        }
        return privateKey;
    }

    /**
     * Get public key for token verification
     */
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            throw new IllegalStateException("Public key not loaded");
        }
        return publicKey;
    }

    /**
     * Load keys from configured source
     */
    private void loadKeys() throws Exception {
        if (jwtConfig.getKeyStorePath() != null) {
            loadKeysFromKeyStore();
        } else if (jwtConfig.getPrivateKeyPath() != null && jwtConfig.getPublicKeyPath() != null) {
            loadKeysFromFiles();
        } else {
            throw new IllegalStateException("No key source configured for asymmetric signing");
        }
    }

    /**
     * Load keys from individual PEM files
     */
    private void loadKeysFromFiles() throws Exception {
        log.debug("Loading keys from files");

        // Load private key
        String privateKeyContent = Files.readString(Paths.get(jwtConfig.getPrivateKeyPath()));
        this.privateKey = parsePrivateKey(privateKeyContent);

        // Load public key
        String publicKeyContent = Files.readString(Paths.get(jwtConfig.getPublicKeyPath()));
        this.publicKey = parsePublicKey(publicKeyContent);

        log.debug("Keys loaded successfully from files");
    }

    /**
     * Load keys from Java KeyStore
     */
    private void loadKeysFromKeyStore() throws Exception {
        log.debug("Loading keys from keystore");

        keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(jwtConfig.getKeyStorePath())) {
            keyStore.load(fis, jwtConfig.getKeyStorePassword().toCharArray());
        }

        // Load private key
        this.privateKey = (PrivateKey) keyStore.getKey(
                jwtConfig.getKeyAlias(),
                jwtConfig.getKeyStorePassword().toCharArray()
        );

        // Load public key from certificate
        Certificate certificate = keyStore.getCertificate(jwtConfig.getKeyAlias());
        this.publicKey = certificate.getPublicKey();

        log.debug("Keys loaded successfully from keystore");
    }

    /**
     * Parse private key from PEM format
     */
    private PrivateKey parsePrivateKey(String keyContent) throws Exception {
        // Remove PEM headers and whitespace
        String privateKeyPEM = keyContent
                .replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Decode Base64
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);

        // Create key spec and generate key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Parse public key from PEM format
     */
    private PublicKey parsePublicKey(String keyContent) throws Exception {
        // Remove PEM headers and whitespace
        String publicKeyPEM = keyContent
                .replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // Decode Base64
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);

        // Create key spec and generate key
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Generate new RSA key pair (for key rotation)
     */
    public KeyPair generateNewKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048); // 2048-bit RSA keys
        return keyGenerator.generateKeyPair();
    }

}

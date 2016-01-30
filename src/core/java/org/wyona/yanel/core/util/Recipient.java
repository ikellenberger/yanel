package org.wyona.yanel.core.util;

/**
 *
 */
public class Recipient {

    private String email;
    private String pgpPublicKey;
    private String smimeCert;

    /**
     * @param email Email address of recipient
     */
    public Recipient(String email) {
        this.email = email;
    }

    /**
     * Get email address of recipient
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set PGP public key
     * @param pgpPublicKey PGP public key block (-----BEGIN PGP PUBLIC KEY BLOCK----- ... -----END PGP PUBLIC KEY BLOCK-----)
     */
    public void setPGPPublicKey(String pgpPublicKey) {
        this.pgpPublicKey = pgpPublicKey;
    }

    /**
     * Get PGP public key block
     * @return PGP public key block (-----BEGIN PGP PUBLIC KEY BLOCK----- ... -----END PGP PUBLIC KEY BLOCK-----)
     */
    public String getPGPPublicKey() {
        return pgpPublicKey;
    }

    /**
     * Set S/MIME certificate
     * @param smimeCert S/MIME certificate (-----BEGIN CERTIFICATE----- ... -----END CERTIFICATE-----)
     */
    public void setSMIMECertificate(String smimeCert) {
        this.smimeCert = smimeCert;
    }

    /**
     *
     * Get S/MIME certificate
     * @return S/MIME certificate (-----BEGIN CERTIFICATE----- ... -----END CERTIFICATE-----)
     */
    public String getSMIMECertificate() {
        return smimeCert;
    }
}

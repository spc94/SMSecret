package com.example.spice.smsecret;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

/**
 * @author JavaDigest
 *
 */
public class RSA {

    /**
     * String to hold name of the encryption algorithm.
     */
    public static final String ALGORITHM = "RSA";

    /**
     * String to hold the name of the private key file.
     */
    public static final String PRIVATE_KEY_FILE = "private.key";

    /**
     * String to hold name of the public key file.
     */
    public static final String PUBLIC_KEY_FILE = "public.key";

    /**
     * Generate key which contains a pair of private and public key using 1024
     * bytes. Store the set of keys in Prvate.key and Public.key files.
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void generateKey(Context context) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            File privateKeyFile = new File(context.getFilesDir(),PRIVATE_KEY_FILE);
            File publicKeyFile = new File(context.getFilesDir(),PUBLIC_KEY_FILE);

            // Create files to store public and private key
            privateKeyFile.createNewFile();
            publicKeyFile.createNewFile();

            // Saving the Public key in a file
            ObjectOutputStream publicKeyOS = new ObjectOutputStream(
                    new FileOutputStream(publicKeyFile));
            publicKeyOS.writeObject(key.getPublic());
            publicKeyOS.close();

            // Saving the Private key in a file encrypted
            ObjectOutputStream privateKeyOS = new ObjectOutputStream(
                    new FileOutputStream(privateKeyFile));
            AES.SecretKeys keys = genKeys("password");
            PrivateKey privateKey = key.getPrivate();
            byte[] k = Base64.encode(privateKey.getEncoded(),Base64.DEFAULT);
            String encodedPrivateKey = new String(k);
            AES.CipherTextIvMac cipher = genCipher(encodedPrivateKey,keys);
            privateKeyOS.writeObject(cipher);
            privateKeyOS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public AES.CipherTextIvMac genCipher(
            String messageToEncrypt, AES.SecretKeys secretKeys){

        try {
            return AES.encrypt(messageToEncrypt, secretKeys);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;

    }

    public AES.SecretKeys genKeyWithSalt(String password, String saltString){
        try {
            Log.d("DEBUG","Salt: "+saltString);
            AES.SecretKeys secretKeys =
                    AES.generateKeyFromPassword(password, saltString);
            return secretKeys;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public AES.SecretKeys genKeys(String password) {

        try {
            byte[] salt = AES.generateSalt();
            String saltString = AES.saltString(salt);
            MainActivity.getInstance().writeToFile("AES.salt",saltString);
            Log.d("DEBUG","Salt: "+saltString);
            AES.SecretKeys secretKeys =
                    AES.generateKeyFromPassword(password, saltString);
            return secretKeys;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * The method checks if the pair of public and private key has been generated.
     *
     * @return flag indicating if the pair of keys were generated.
     */
    public static boolean areKeysPresent(Context context) {

        File privateKey = new File(context.getFilesDir(),PRIVATE_KEY_FILE);
        File publicKey = new File(context.getFilesDir(),PUBLIC_KEY_FILE);

        if (privateKey.exists() && publicKey.exists()) {
            return true;
        }
        return false;
    }

    /**
     * Encrypt the plain text using public key.
     *
     * @param text
     *          : original plain text
     * @param key
     *          :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static byte[] encrypt(String text, PublicKey key) {
        byte[] cipherText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    /**
     * Decrypt text using private key.
     *
     * @param text
     *          :encrypted text
     * @param key
     *          :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static String decrypt(byte[] text, PrivateKey key) {
        byte[] dectyptedText = null;
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ALGORITHM);

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            dectyptedText = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new String(dectyptedText);
    }


    /**
     * Test the EncryptionUtil
     */
    public void main(String[] args) {

        try {

            // Check if the pair of keys are present else generate those.
            if (!areKeysPresent(MainActivity.getInstance())) {
                // Method generates a pair of keys using the RSA algorithm and stores it
                // in their respective files
                generateKey(MainActivity.getInstance());
            }

            final String originalText = "Text to be encrypted ";
            ObjectInputStream inputStream = null;

            // Encrypt the string using the public key
            inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
            final PublicKey publicKey = (PublicKey) inputStream.readObject();
            final byte[] cipherText = encrypt(originalText, publicKey);

            // Decrypt the cipher text using the private key.
            inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
            final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
            final String plainText = decrypt(cipherText, privateKey);

            // Printing the Original, Encrypted and Decrypted Text
            System.out.println("Original: " + originalText);
            System.out.println("Encrypted: " +cipherText.toString());
            System.out.println("Decrypted: " + plainText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
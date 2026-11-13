package com.atm.servidor.service;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Gera e confere hashes de PIN no formato iteracoes:salt:hash. */
public final class Senhas {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 100_000;
    private static final int BITS = 256;

    private Senhas() {
    }

    public static String gerarHash(String pin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        Base64.Encoder base64 = Base64.getEncoder();
        return ITERACOES + ":" + base64.encodeToString(salt) + ":"
                + base64.encodeToString(derivar(pin, salt, ITERACOES));
    }

    public static boolean confere(String pin, String hashGuardado) {
        String[] partes = hashGuardado.split(":");
        Base64.Decoder base64 = Base64.getDecoder();
        byte[] esperado = base64.decode(partes[2]);
        byte[] obtido = derivar(pin, base64.decode(partes[1]), Integer.parseInt(partes[0]));
        return MessageDigest.isEqual(esperado, obtido);
    }

    private static byte[] derivar(String pin, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, iteracoes, BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Imprime o hash de um PIN, para cadastrar novos clientes no banco. */
    public static void main(String[] args) {
        System.out.println(gerarHash(args[0]));
    }
}

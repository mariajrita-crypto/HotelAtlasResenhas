package com.hotel.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hotel.model.Cliente;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/** Persistência local de demonstração. Os dados ficam fora da pasta do projeto. */
public final class ClienteService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Cliente>>() {}.getType();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final Path ARQUIVO = resolveDataDirectory().resolve("clientes.json");

    private static Path resolveDataDirectory() {
        String home = System.getProperty("user.home");
        if (home == null || home.isBlank()) home = System.getProperty("user.dir", ".");
        return Path.of(home).toAbsolutePath().normalize().resolve(".hotel-atlas");
    }

    private ClienteService() { }

    public static synchronized void cadastrar(String cpfInformado, String telefoneInformado, String senha) throws IOException {
        String cpf = somenteNumeros(cpfInformado);
        String telefone = somenteNumeros(telefoneInformado);
        CadastroValidator.Erro erro = CadastroValidator.validar(cpf, telefone, senha, senha);
        if (erro != null) throw new IllegalArgumentException(erro.mensagem());

        List<Cliente> clientes = carregar();
        if (clientes.stream().anyMatch(c -> cpf.equals(c.getCpf()))) {
            throw new IllegalArgumentException("Já existe uma conta cadastrada com esse CPF.");
        }
        byte[] saltBytes = new byte[16];
        RANDOM.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        String hash = hashSenha(senha, saltBytes);
        clientes.add(new Cliente(cpf, telefone, salt, hash));
        salvar(clientes);
    }

    public static synchronized boolean autenticar(String cpfInformado, String senha) throws IOException {
        String cpf = somenteNumeros(cpfInformado);
        if (cpf.isBlank() || senha == null) return false;
        for (Cliente cliente : carregar()) {
            if (cpf.equals(cliente.getCpf())) {
                byte[] salt = Base64.getDecoder().decode(cliente.getSalt());
                byte[] esperado = Base64.getDecoder().decode(cliente.getSenhaHash());
                byte[] recebido = Base64.getDecoder().decode(hashSenha(senha, salt));
                return MessageDigest.isEqual(esperado, recebido);
            }
        }
        return false;
    }

    public static String caminhoArquivo() { return ARQUIVO.toString(); }

    private static List<Cliente> carregar() throws IOException {
        if (!Files.exists(ARQUIVO)) return new ArrayList<>();
        String json = Files.readString(ARQUIVO, StandardCharsets.UTF_8);
        List<Cliente> clientes = GSON.fromJson(json, LIST_TYPE);
        return clientes == null ? new ArrayList<>() : new ArrayList<>(clientes);
    }

    private static void salvar(List<Cliente> clientes) throws IOException {
        Files.createDirectories(ARQUIVO.getParent());
        Path temporario = ARQUIVO.resolveSibling(ARQUIVO.getFileName() + ".tmp");
        Files.writeString(temporario, GSON.toJson(clientes), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        try {
            Files.move(temporario, ARQUIVO, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporario, ARQUIVO, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String hashSenha(String senha, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger a senha.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String somenteNumeros(String valor) { return valor == null ? "" : valor.replaceAll("\\D", ""); }

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) return false;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int digito = 11 - soma % 11;
        if (digito > 9) digito = 0;
        if (digito != cpf.charAt(9) - '0') return false;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        digito = 11 - soma % 11;
        if (digito > 9) digito = 0;
        return digito == cpf.charAt(10) - '0';
    }
}

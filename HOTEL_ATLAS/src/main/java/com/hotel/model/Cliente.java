package com.hotel.model;

/** Dados de cadastro do cliente. A senha nunca é armazenada em texto puro. */
public class Cliente {
    private String cpf;
    private String telefone;
    private String salt;
    private String senhaHash;
    private long criadoEm;

    public Cliente() { }
    public Cliente(String cpf, String telefone, String salt, String senhaHash) {
        this.cpf = cpf;
        this.telefone = telefone;
        this.salt = salt;
        this.senhaHash = senhaHash;
        this.criadoEm = System.currentTimeMillis();
    }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public String getSalt() { return salt; }
    public String getSenhaHash() { return senhaHash; }
    public long getCriadoEm() { return criadoEm; }
}

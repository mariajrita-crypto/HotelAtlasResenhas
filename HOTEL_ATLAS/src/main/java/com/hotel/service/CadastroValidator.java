package com.hotel.service;

/** Regras de validação do cadastro de cliente (sem dependência de JavaFX, fácil de testar). */
public final class CadastroValidator {
    public static final int CPF_DIGITOS = 11;
    public static final int TELEFONE_MAX_DIGITOS = 11;   // DDD (2) + celular (9)
    public static final int TELEFONE_MIN_DIGITOS = 10;   // DDD (2) + fixo (8)
    public static final int SENHA_MIN = 8;

    /** Campos do formulário, usados para marcar em vermelho o campo com problema. */
    public enum Campo { CPF, TELEFONE, SENHA, CONFIRMAR }

    /** Resultado de uma validação: campo com erro + mensagem. */
    public record Erro(Campo campo, String mensagem) { }

    private CadastroValidator() { }

    public static String somenteNumeros(String v) { return v == null ? "" : v.replaceAll("\\D", ""); }

    /** Retorna o primeiro erro encontrado (na ordem da tela) ou null se tudo estiver certo. */
    public static Erro validar(String cpf, String telefone, String senha, String confirmar) {
        Erro e = validarCpf(cpf);
        if (e != null) return e;
        e = validarTelefone(telefone);
        if (e != null) return e;
        return validarSenha(senha, confirmar);
    }

    public static Erro validarCpf(String cpfCampo) {
        if (cpfCampo == null || cpfCampo.isBlank())
            return new Erro(Campo.CPF, "Preencha o CPF.");
        if (!cpfCampo.matches("\\d+"))
            return new Erro(Campo.CPF, "O CPF deve ter somente números.");
        if (cpfCampo.length() > CPF_DIGITOS)
            return new Erro(Campo.CPF, "O CPF deve ter no máximo " + CPF_DIGITOS + " números.");
        if (cpfCampo.length() < CPF_DIGITOS)
            return new Erro(Campo.CPF, "O CPF precisa ter " + CPF_DIGITOS + " números (faltam " + (CPF_DIGITOS - cpfCampo.length()) + ").");
        if (!ClienteService.cpfValido(cpfCampo))
            return new Erro(Campo.CPF, "CPF inválido. Confira os números digitados.");
        return null;
    }

    public static Erro validarTelefone(String telCampo) {
        if (telCampo == null || telCampo.isBlank())
            return new Erro(Campo.TELEFONE, "Preencha o telefone com DDD.");
        if (!telCampo.matches("\\d+"))
            return new Erro(Campo.TELEFONE, "O telefone deve ter somente números.");
        if (telCampo.length() < TELEFONE_MIN_DIGITOS || telCampo.length() > TELEFONE_MAX_DIGITOS)
            return new Erro(Campo.TELEFONE, "Telefone incompleto: informe DDD + número (10 ou 11 números).");
        if (!telCampo.matches("[1-9][1-9]\\d+"))
            return new Erro(Campo.TELEFONE, "DDD inválido. Exemplo: 48999998888.");
        if (telCampo.length() == 11 && telCampo.charAt(2) != '9')
            return new Erro(Campo.TELEFONE, "Celular com 11 números deve começar com 9 depois do DDD.");
        return null;
    }

    public static Erro validarSenha(String senha, String confirmar) {
        if (senha == null || senha.isEmpty())
            return new Erro(Campo.SENHA, "Preencha a senha.");
        if (senha.length() < SENHA_MIN)
            return new Erro(Campo.SENHA, "A senha precisa ter no mínimo " + SENHA_MIN + " caracteres (tem " + senha.length() + ").");
        if (confirmar == null || confirmar.isEmpty())
            return new Erro(Campo.CONFIRMAR, "Confirme a senha.");
        if (!senha.equals(confirmar))
            return new Erro(Campo.CONFIRMAR, "As senhas não coincidem.");
        return null;
    }
}

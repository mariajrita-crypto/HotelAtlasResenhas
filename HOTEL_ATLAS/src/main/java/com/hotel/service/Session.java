package com.hotel.service;

/** Sessão simples da aplicação; para clientes, o identificador é o CPF. */
public final class Session {
    private static String name;
    private static String identifier;
    private static String role;
    private Session() { }
    public static void login(String userName, String userIdentifier, String userRole) { name=userName; identifier=userIdentifier; role=userRole; }
    public static String getName(){return name;}
    /** Mantido por compatibilidade com controladores antigos. */
    public static String getEmail(){return identifier;}
    public static String getCpf(){return isEmployee()?null:identifier;}
    public static String getIdentifier(){return identifier;}
    public static String getRole(){return role;}
    public static boolean isEmployee(){return "FUNCIONARIO".equals(role);}
    public static void clear(){name=null;identifier=null;role=null;}
}

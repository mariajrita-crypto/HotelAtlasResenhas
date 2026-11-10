# Reservas vinculadas ao CPF — Hotel Atlas

## O que mudou
- Cada reserva guarda o CPF normalizado da conta autenticada como titular.
- A aba **Minhas reservas** consulta apenas reservas cujo CPF corresponde ao CPF da sessão atual.
- O cancelamento verifica novamente o CPF proprietário, o código, o status e a data de entrada.
- Cada reserva ganhou a ação **Ver detalhes**, com período, quarto, hóspedes, pagamento, valor e status. O CPF é mascarado na tela.
- As reservas agora são persistidas em JSON em `%USERPROFILE%\\.hotel-atlas\\reservas.json` (Windows) ou `~/.hotel-atlas/reservas.json` (Linux/macOS).
- A gravação usa arquivo temporário e substituição atômica quando suportada.
- Os funcionários continuam usando o identificador de funcionário; `Session.getEmail()` foi mantido como alias de compatibilidade, mas para clientes deve-se usar `Session.getCpf()`.

## Como testar
1. Entre com um cliente cadastrado por CPF.
2. Faça uma reserva e confirme o código exibido.
3. Feche totalmente o aplicativo e abra novamente.
4. Entre com o mesmo CPF e abra **Minhas reservas**: a reserva deve continuar lá.
5. Entre com outro CPF: a reserva anterior não deve aparecer.
6. Teste **Ver detalhes** e o cancelamento de uma reserva futura.
7. Confira o arquivo `reservas.json` no diretório `.hotel-atlas` do usuário.

## Observações
- A persistência é local ao computador/usuário do sistema operacional. Não sincroniza entre computadores e não substitui um banco de dados central.
- O JSON contém CPF e dados de hospedagem em texto legível. Use dados fictícios em demonstrações e proteja o acesso ao computador/arquivo.
- O sistema não realiza cobrança real.
- O projeto usa Java 21 e Maven. Execute `mvn clean test` e depois `mvn javafx:run` no IntelliJ/terminal com Maven instalado.

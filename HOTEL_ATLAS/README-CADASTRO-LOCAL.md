# Cadastro e login de clientes — Hotel Atlas

## Como funciona
- O cliente cria conta informando CPF, telefone com DDD e senha.
- O login do cliente é feito com CPF e senha.
- O funcionário continua entrando pelo e-mail de demonstração `funcionario@atlas.com` e senha `123456`.
- O CPF é validado pelos dígitos verificadores e não pode ser cadastrado duas vezes.
- O telefone deve conter 10 ou 11 dígitos.
- A senha deve ter pelo menos 8 caracteres e é armazenada como hash PBKDF2-HMAC-SHA256 com salt aleatório. A senha original não é gravada no arquivo.

## Onde os dados ficam
O aplicativo cria automaticamente:

`%USERPROFILE%\\.hotel-atlas\\clientes.json`

No Windows, isso normalmente corresponde a `C:\\Users\\SEU_USUARIO\\.hotel-atlas\\clientes.json`.

O arquivo é criado fora da pasta do projeto, então os cadastros continuam existindo depois de fechar o programa e não são apagados ao substituir/compilar o projeto. Faça backup desse arquivo se precisar preservar os cadastros.

## Dependência
O projeto usa Gson, declarada no `pom.xml`. No IntelliJ, abra a janela Maven e clique em **Sync Project** para baixar a dependência.

## Observação de segurança
Este armazenamento em arquivo é adequado apenas para demonstração escolar/local. CPF e telefone continuam sendo dados pessoais; use dados fictícios nos testes. Para um sistema real, não deixe dados pessoais em um arquivo local sem controles de acesso, proteção do dispositivo, política de privacidade e armazenamento seguro. O hash protege a senha, mas não criptografa CPF/telefone.

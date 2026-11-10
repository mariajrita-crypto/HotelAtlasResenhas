# Hotel Atlas — área do cliente

## O que foi melhorado
- Busca de quartos por check-in, check-out e quantidade de hóspedes.
- Filtro por categoria (Standard, Superior, Família e Suíte).
- Ordenação por menor preço, maior preço ou capacidade.
- Cartões com número do quarto, categoria, qualidade, tamanho, comodidades, diária e total do período.
- Resumo antes de confirmar, forma de pagamento pretendida e observações opcionais.
- Validação das datas, capacidade, conta do cliente e disponibilidade novamente no momento da confirmação.
- Aba **Minhas reservas**, com histórico, código, período, hóspedes, valor, status e opção de cancelar reservas futuras.
- Quarto liberado novamente quando uma reserva futura é cancelada.

## Como abrir no IntelliJ
1. Extraia o ZIP.
2. No IntelliJ, escolha **File > Open** e selecione a pasta `HOTEL_ATLAS` (a pasta que contém `pom.xml`).
3. Configure um JDK 21: **File > Project Structure > Project > SDK > Add SDK > Download JDK > 21**.
4. Em **Project Structure > Modules**, confirme que o módulo usa o mesmo SDK 21.
5. Abra o `pom.xml` e clique em **Load Maven Changes** se o IntelliJ pedir.
6. Execute `com.example.hotel_atlas.HelloApplication` ou use a configuração Maven `javafx:run`.

## Login demonstrativo
- Cliente: `cliente@atlas.com` / `123456`
- Funcionário: `funcionario@atlas.com` / `123456`

## Limitações importantes desta versão
Os dados ainda ficam em memória: reservas desaparecem quando o programa é fechado. O pagamento é apenas uma preferência registrada; o aplicativo não cobra cartão nem gera PIX. Para uso real, é necessário conectar MySQL, criar autenticação segura e implementar regras de pagamento e política de cancelamento do hotel.


## Atualização visual e validações (tema noturno suave)

- A interface agora usa uma paleta grafite/azul-ardósia com detalhes champanhe, evitando fundos brancos intensos.
- Campos, menus, calendários, abas, tabelas, cartões e diálogos receberam estilos escuros consistentes.
- A validação de reservas também é feita na camada de dados: nome, e-mail, datas, capacidade, forma de pagamento, limite de observações e disponibilidade são conferidos antes de criar a reserva.
- Ao alterar datas ou quantidade de hóspedes, a seleção anterior e a confirmação de revisão são limpas para evitar confirmar um resumo desatualizado.
- Mensagens de erro de carregamento não exibem mais caminhos internos do computador.

**Limitações atuais:** os usuários de demonstração continuam fixos no código e os quartos/reservas ficam em memória. Não há banco de dados, cadastro real, autenticação segura nem processamento de pagamentos.
